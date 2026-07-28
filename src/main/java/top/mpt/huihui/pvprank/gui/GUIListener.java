package top.mpt.huihui.pvprank.gui;

import jdk.jpackage.internal.Log;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.utils.LogUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static top.mpt.huihui.pvprank.PVPRank.normal;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String rawTitle = event.getView().getTitle();
        String title = ChatColor.stripColor(rawTitle);
        if (!GUIManager.isPluginGUI(title)) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // ====== 通用返回按钮（BARRIER + "返回"） ======
        if (clicked.getType() == Material.BARRIER) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && ChatColor.stripColor(meta.getDisplayName()).contains("返回")) {
                GUIManager.goBack(player);
                return;
            }
        }

        // ====== 权限选择 ======
        if (title.equals(ChatColor.stripColor(GUIManager.PERM_CHOOSE))) {
            handlePermChoose(player, clicked);
            return;
        }

        // ====== 团队PVP选择 ======
        if (title.contains(ChatColor.stripColor(GUIManager.TPVP_TITLE))) {
            handleTeamPvpSelect(player, clicked, title);
            return;
        }

        // ====== 主菜单 ======
        if (title.contains(ChatColor.stripColor(GUIManager.MAIN_TITLE))) {
            handleMainClick(player, clicked);
            return;
        }

        // ====== 团队管理 ======
        if (title.contains(ChatColor.stripColor(GUIManager.TEAM_TITLE))) {
            handleTeamClick(player, clicked);
            return;
        }

        // ====== PlayerSelectGUI（翻页玩家列表） ======
        if (PlayerSelectGUI.isPlayerSelectGUI(title)) {
            handlePlayerSelect(player, clicked, rawTitle, title);
            return;
        }
    }

    // ====== PlayerSelectGUI 点击 ======
    private void handlePlayerSelect(Player player, ItemStack item, String rawTitle, String strippedTitle) {
        int page = GUIManager.pageState.getOrDefault(player.getUniqueId(), 0);
        String base = PlayerSelectGUI.getBaseTitle(strippedTitle);

        if (item.getType() == Material.ARROW) {
            String arrowName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (arrowName.contains("上一页")) page--;
            else if (arrowName.contains("下一页")) page++;
            GUIManager.pageState.put(player.getUniqueId(), page);
            // 重新获取玩家列表并打开
            List<Player> list = getPlayerListForTitle(player, base);
            PlayerSelectGUI.open(player, list, base, page,
                    GUIManager.backTargets.get(player.getUniqueId()));
            return;
        }
        if (item.getType() == Material.PAPER || item.getType() != Material.PLAYER_HEAD) return;

        String targetName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) return;

        // 根据标题路由行为
        String mainTitle = ChatColor.stripColor(GUIManager.MAIN_TITLE);
        String teamTitle = ChatColor.stripColor(GUIManager.TEAM_TITLE);
        String soloTitle = ChatColor.stripColor(GUIManager.SOLO_TITLE);
        String inviteTitle = ChatColor.stripColor(GUIManager.INVITE_TITLE);
        String kickTitle = ChatColor.stripColor(GUIManager.KICK_TITLE);
        String permTitle = ChatColor.stripColor(GUIManager.PERM_TITLE);

        LogUtils.info(base);
        if (base.contains(soloTitle)) {
            player.closeInventory();
            Bukkit.dispatchCommand(player, "pvprank solo " + targetName);
        } else if (base.contains(inviteTitle)) {
            player.closeInventory();
            Bukkit.dispatchCommand(player, "pvprank invite " + targetName);
        } else if (base.contains(kickTitle)) {
            player.closeInventory();
            Bukkit.dispatchCommand(player, "pvprank kick " + targetName);
        } else if (base.contains(permTitle)) {
            player.closeInventory();
            GUIManager.openPermChooseGUI(player, target.getUniqueId());
        }
    }

    // 根据标题获取对应玩家列表
    private List<Player> getPlayerListForTitle(Player player, String base) {
        String kickTitle = ChatColor.stripColor(GUIManager.KICK_TITLE);
        String permTitle = ChatColor.stripColor(GUIManager.PERM_TITLE);
        if (base.contains(kickTitle) || base.contains(permTitle)) {
            Integer tid = TeamExecutor.getPlayerNormalTeamId(player);
            if (tid == null) return new ArrayList<>();
            List<Player> members = TeamExecutor.getOnlineTeamMembers(tid);
            members.remove(player);
            return members;
        }
        // solo/invite: 全服玩家
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.remove(player);
        return online;
    }

    // ====== 主菜单 ======
    private void handleMainClick(Player player, ItemStack item) {
        if (item.getType() == Material.DIAMOND_SWORD) {
            GUIManager.openSoloGUI(player);
        } else if (item.getType() == Material.SHIELD) {
            GUIManager.openTeamMenu(player);
        }
    }

    // ====== 团队管理 ======
    private void handleTeamClick(Player player, ItemStack item) {
        Material type = item.getType();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String name = ChatColor.stripColor(meta.getDisplayName());

        // 创建团队
        if (type == Material.ANVIL) {
            GUIManager.openAnvil(player);
            return;
        }
        // 团队信息
        if (type == Material.BOOKSHELF) {
            GUIManager.openTeamInfo(player);
            return;
        }
        // 邀请玩家
        if (type == Material.PLAYER_HEAD && name.contains("邀请")) {
            GUIManager.openInviteGUI(player);
            return;
        }
        // 踢出玩家
        if (type == Material.BARRIER) {
            GUIManager.openKickGUI(player);
            return;
        }
        // 解散团队
        if (type == Material.TNT) {
            player.closeInventory();
            Bukkit.dispatchCommand(player, "pvprank breakup");
            return;
        }
        // 设置权限
        if (type == Material.WRITABLE_BOOK) {
            GUIManager.openPermTargetGUI(player);
            return;
        }
        // 团队PVP
        if (type == Material.IRON_SWORD && name.contains("PVP")) {
            GUIManager.backTargets.put(player.getUniqueId(), "team");
            GUIManager.openTeamPvpSelectGUI(player);
            return;
        }
    }

    // ====== 权限选择 ======
    private void handlePermChoose(Player player, ItemStack item) {
        UUID targetUuid = GUIManager.permTargets.remove(player.getUniqueId());
        if (targetUuid == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String perm = ChatColor.stripColor(meta.getDisplayName()).toLowerCase();
        if (!perm.equals("owner") && !perm.equals("operator") && !perm.equals("member")) return;
        player.closeInventory();
        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null) {
            Bukkit.dispatchCommand(player, "pvprank setpermission " + target.getName() + " " + perm);
        }
    }

    // ====== 团队PVP选择 ======
    private void handleTeamPvpSelect(Player player, ItemStack item, String strippedTitle) {
        int page = GUIManager.teampvpPage.getOrDefault(player.getUniqueId(), 0);
        if (item.getType() == Material.ARROW) {
            String arrowName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (arrowName.contains("上一页")) page--;
            else if (arrowName.contains("下一页")) page++;
            GUIManager.teampvpPage.put(player.getUniqueId(), page);
            GUIManager.openTeamPvpSelectGUI(player);
            return;
        }
        if (item.getType() == Material.PAPER) return;
        if (item.getType() == Material.NAME_TAG) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null || meta.getLore() == null || meta.getLore().isEmpty()) return;
            String loreLine = meta.getLore().get(0).replace("§7", "");
            String idStr = loreLine.replaceAll("[^0-9]", " ").trim().split(" ")[0];
            try {
                int targetTeamId = Integer.parseInt(idStr);
                Integer myTeamId = TeamExecutor.getPlayerNormalTeamId(player);
                if (myTeamId != null) {
                    player.closeInventory();
                    LogUtils.info("pvprank teampvp " + myTeamId + " " + targetTeamId);
                    Bukkit.dispatchCommand(player, "pvprank teampvp " + myTeamId + " " + targetTeamId);
                }
            } catch (NumberFormatException ignored) {}
        }
    }
}
