package top.mpt.huihui.pvprank.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import net.wesjd.anvilgui.AnvilGUI;
import top.mpt.huihui.pvprank.PVPRank;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.manager.Team;
import top.mpt.huihui.pvprank.utils.ChatUtils;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

public class GUIManager {

    public static final String MAIN_TITLE = "§bPVP 菜单";
    public static final String TEAM_TITLE = "§b团队管理";
    public static final String SOLO_TITLE = "§b选择对手";
    public static final String INVITE_TITLE = "§b邀请玩家入队";
    public static final String KICK_TITLE = "§b选择踢出成员";
    public static final String PERM_TITLE = "§b选择权限目标";
    public static final String PERM_CHOOSE = "§b选择权限";
    public static final String TPVP_TITLE = "§b选择团队对战";

    // 翻页状态: player UUID -> current page
    public static final Map<UUID, Integer> pageState = new HashMap<>();
    // 权限目标: clicker UUID -> target UUID
    public static final Map<UUID, UUID> permTargets = new HashMap<>();
    // 返回目标: player UUID -> "main"/"team"
    public static final Map<UUID, String> backTargets = new HashMap<>();
    // 团队PVP分页
    public static final Map<UUID, Integer> teampvpPage = new HashMap<>();

    // ========== 主菜单 ==========
    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, MAIN_TITLE);
        inv.setItem(2, createItem(Material.DIAMOND_SWORD, "§a单人PVP", "§7点击选择对手"));
        PlayerData playerData = TeamExecutor.getPlayerData(player.getUniqueId());
        // 创建SimpleDateFormat对象，指定目标格式
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        List<String> lore = Arrays.asList(
                "§7所属团队ID: " + playerData.getTeamId(),
                "§7团队内权限:" + playerData.getPermission(),
                "§7入队时间: " + format.format(new Date(playerData.getJoinTime())),
                "§7个人积分: " + playerData.getPersonalScore()
        );
        inv.setItem(4, createMultiLoreItem(Material.BOOK, "§e个人信息", lore));
        inv.setItem(6, createItem(Material.SHIELD, "§a团队管理", "§7点击管理团队"));
        player.openInventory(inv);
    }

    // ========== 单人PVP → PlayerSelectGUI ==========
    public static void openSoloGUI(Player player) {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.remove(player);
        pageState.put(player.getUniqueId(), 0);
        PlayerSelectGUI.open(player, online, SOLO_TITLE, 0, "main");
    }

    // ========== 团队管理 ==========
    public static void openTeamMenu(Player player) {
        Integer teamId = TeamExecutor.getPlayerNormalTeamId(player);
        if (teamId == null) {
            Inventory inv = Bukkit.createInventory(null, 9, TEAM_TITLE);
            inv.setItem(4, createItem(Material.ANVIL, "§a创建团队", "§7点击创建新团队"));
            player.openInventory(inv);
            return;
        }
        PlayerData pd = TeamExecutor.getPlayerData(player.getUniqueId());
        String perm = pd != null ? pd.getPermission() : "member";
        Team team = TeamExecutor.getTeamData(teamId);
        Inventory inv = Bukkit.createInventory(null, 27, TEAM_TITLE);
        int slot = 10;
        inv.setItem(slot++, createItem(Material.BOOKSHELF, "§e团队信息",
                "§7ID: " + teamId + " | 名称: " + (team != null ? team.getName() : "?")
                + " | 积分: " + (team != null ? team.getScore() : 0)));
        if ("owner".equals(perm)) {
            inv.setItem(slot++, createItem(Material.PLAYER_HEAD, "§a邀请玩家", "§7邀请其他玩家加入团队"));
            inv.setItem(slot++, createItem(Material.BARRIER, "§c踢出玩家", "§7将成员踢出团队"));
            inv.setItem(slot++, createItem(Material.TNT, "§c解散团队", "§7永久解散当前团队"));
            inv.setItem(slot++, createItem(Material.WRITABLE_BOOK, "§b设置权限", "§7修改成员权限"));
            inv.setItem(slot++, createItem(Material.IRON_SWORD, "§6团队PVP", "§7向其他团队发起挑战"));
        } else if ("operator".equals(perm)) {
            inv.setItem(slot++, createItem(Material.BARRIER, "§c踢出玩家", "§7将成员踢出团队"));
        }
        inv.setItem(16,createBackButton());
        player.openInventory(inv);
    }


    // ========== 邀请入队 → PlayerSelectGUI ==========
    public static void openInviteGUI(Player player) {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.remove(player);
        pageState.put(player.getUniqueId(), 0);
        PlayerSelectGUI.open(player, online, INVITE_TITLE, 0, "team");
    }

    // ========== 踢出成员 → PlayerSelectGUI(team members) ==========
    public static void openKickGUI(Player player) {
        Integer teamId = TeamExecutor.getPlayerNormalTeamId(player);
        if (teamId == null) return;
        List<Player> members = TeamExecutor.getOnlineTeamMembers(teamId);
        members.remove(player);
        pageState.put(player.getUniqueId(), 0);
        PlayerSelectGUI.open(player, members, KICK_TITLE, 0, "team");
    }

    // ========== 权限目标 → PlayerSelectGUI(team members) ==========
    public static void openPermTargetGUI(Player player) {
        Integer teamId = TeamExecutor.getPlayerNormalTeamId(player);
        if (teamId == null) return;
        List<Player> members = TeamExecutor.getOnlineTeamMembers(teamId);
        members.remove(player);
        pageState.put(player.getUniqueId(), 0);
        PlayerSelectGUI.open(player, members, PERM_TITLE, 0, "team");
    }

    // ========== 权限选择(owner/operator/member) ==========
    public static void openPermChooseGUI(Player player, UUID targetUuid) {
        permTargets.put(player.getUniqueId(), targetUuid);
        Inventory inv = Bukkit.createInventory(null, 9, PERM_CHOOSE);
        inv.setItem(1, createItem(Material.GOLDEN_HELMET, "§6owner", "§7团队所有者"));
        inv.setItem(4, createItem(Material.IRON_HELMET, "§boperator", "§7团队管理员"));
        inv.setItem(7, createItem(Material.LEATHER_HELMET, "§amember", "§7普通成员"));
        inv.setItem(8, createBackButton());
        player.openInventory(inv);
    }

    // ========== 团队信息详情 ==========
    public static void openTeamInfo(Player player) {
        Integer teamId = TeamExecutor.getPlayerNormalTeamId(player);
        if (teamId == null) return;
        Team team = TeamExecutor.getTeamData(teamId);
        if (team == null) return;
        List<PlayerData> members = TeamExecutor.getTeamMembers(teamId);
        Inventory inv = Bukkit.createInventory(null, 27, "§b团队信息 #" + teamId);
        inv.setItem(10, createItem(Material.NAME_TAG, "§e团队名: " + team.getName(), null));
        inv.setItem(11, createItem(Material.PAPER, "§e团队ID: " + teamId, null));
        inv.setItem(12, createItem(Material.EMERALD, "§e积分: " + team.getScore(), null));
        inv.setItem(13, createItem(Material.IRON_SWORD, "§e战斗中: " + (team.isInBattle() ? "§c是" : "§a否"), null));
        inv.setItem(14, createItem(Material.ENDER_PEARL, "§e对手ID: " + (team.getOpponentTeamId() != null ? team.getOpponentTeamId() : "无"), null));
        int mSlot = 16;
        for (PlayerData m : members) {
            if (mSlot >= 26) break;
            Player mp = Bukkit.getPlayer(UUID.fromString(m.getUuid()));
            String status = mp != null && mp.isOnline() ? "§a在线" : "§7离线";
            inv.setItem(mSlot++, createItem(Material.PLAYER_HEAD,
                    "§a" + m.getPlayerName(),
                    "§7权限: " + m.getPermission() + " | " + status));
        }
        inv.setItem(26, createBackButton());
        player.openInventory(inv);
    }

    // ========== 铁砧创建团队 ==========
    public static void openAnvil(Player player) {
        new AnvilGUI.Builder()
                .plugin(PVPRank.instance)
                .title("输入团队名称")
                .itemLeft(new ItemStack(Material.PAPER))
                .text("在此输入团队名称")
                .onClick((slot, stateSnapshot) -> {
                    // 只处理结果槽（点击输出物品）
                    if (slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList(); // 不做任何操作
                    }
                    String name = stateSnapshot.getText().trim();
                    if (name.isEmpty() || name.contains("在此输入")) {
                        player.sendMessage("§c请输入有效的团队名称！");
                        // 保留输入框文字，让玩家重试
                        return Collections.singletonList(
                                AnvilGUI.ResponseAction.replaceInputText("在此输入团队名称")
                        );
                    }
                    // 执行创建逻辑（同步）
                    try {
                        int teamId = PVPRank.teamPlayerDAO.getLowestAvailableTeamId();
                        TeamExecutor.addTeam(teamId, name, player);
                        // 关闭界面
                        return Collections.singletonList(AnvilGUI.ResponseAction.close());
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage("§c创建失败，请查看控制台错误。");
                        // 让玩家可以重试
                        return Collections.singletonList(
                                AnvilGUI.ResponseAction.replaceInputText("重试")
                        );
                    }
                })
                .open(player);
    }


    // ========== 团队PVP → 显示所有团队编号 ==========
    public static void openTeamPvpSelectGUI(Player player) {
        Integer myTeamId = TeamExecutor.getPlayerNormalTeamId(player);
        if (myTeamId == null) return;
        List<Team> allTeams = TeamExecutor.getAllTeams();
        // 过滤掉自己的团队
        List<Team> filtered = new ArrayList<>();
        for (Team t : allTeams) {
            if (t.getId() != myTeamId && !t.isInBattle()) {
                filtered.add(t);
            }
        }
        int totalPerPage = 45;
        int totalPages = (filtered.size() + totalPerPage - 1) / totalPerPage;
        if (totalPages == 0) totalPages = 1;
        int page = teampvpPage.getOrDefault(player.getUniqueId(), 0);
        if (page >= totalPages) page = totalPages - 1;
        teampvpPage.put(player.getUniqueId(), page);

        Inventory inv = Bukkit.createInventory(null, 54,
                TPVP_TITLE + " - 第" + (page + 1) + "/" + totalPages + "页");
        int start = page * totalPerPage;
        int end = Math.min(start + totalPerPage, filtered.size());
        for (int i = start; i < end; i++) {
            Team t = filtered.get(i);
            inv.setItem(i - start, createItem(Material.NAME_TAG,
                    "§e" + t.getName(),
                    "§7ID: " + t.getId() + " | 积分: " + t.getScore()));
        }
        // 翻页 + 返回
        if (page > 0) inv.setItem(45, createItem(Material.ARROW, "§a上一页", null));
        inv.setItem(49, createItem(Material.PAPER, "§e第 " + (page + 1) + " / " + totalPages + " 页", null));
        if (page < totalPages - 1) inv.setItem(53, createItem(Material.ARROW, "§a下一页", null));
        inv.setItem(53, createBackButton());
        player.openInventory(inv);
    }

    // ========== 返回上一级 ==========
    public static void goBack(Player player) {
        String target = backTargets.remove(player.getUniqueId());
        if (target == null) {
            openMainMenu(player);
            return;
        }
        if (target.equals("main")) {
            openMainMenu(player);
        } else if (target.equals("team")) {
            openTeamMenu(player);
        }
    }

    // ========== 判断插件GUI ==========
    public static boolean isPluginGUI(String title) {
        String s = ChatColor.stripColor(title);
        if (s.contains(ChatColor.stripColor(MAIN_TITLE))) return true;
        if (s.contains(ChatColor.stripColor(TEAM_TITLE))) return true;
        if (s.contains(ChatColor.stripColor(PERM_CHOOSE))) return true;
        if (s.contains("团队信息")) return true;
        if (s.contains(ChatColor.stripColor(TPVP_TITLE))) return true;
        return PlayerSelectGUI.isPlayerSelectGUI(title);
    }

    // ========== 工具方法 ==========
    public static ItemStack createBackButton() {
        return createItem(Material.BARRIER, "§c返回", "§7返回上一级菜单");
    }

    public static ItemStack createItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(Collections.singletonList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createMultiLoreItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
