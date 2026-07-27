package top.mpt.huihui.pvprank.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class PlayerSelectGUI {

    public static final int MAX_SLOTS = 54;
    public static final int PLAYER_SLOTS = 45;  // 0-44
    public static final int PREV_SLOT = 45;
    public static final int PAGE_SLOT = 48;
    public static final int NEXT_SLOT = 50;
    public static final int BACK_SLOT = 53;

    /**
     * 打开翻页玩家选择UI
     * @param player 打开者
     * @param allPlayers 全部可选玩家列表
     * @param title GUI 标题
     * @param page 页码(0开始)
     */
    public static void open(Player player, List<Player> allPlayers, String title, int page, String backTarget) {
        int totalPages = (allPlayers.size() + PLAYER_SLOTS - 1) / PLAYER_SLOTS;
        if (totalPages == 0) totalPages = 1;
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(null, MAX_SLOTS, title + " - 第" + (page + 1) + "/" + totalPages + "页");
        // ... heads ...
        int start = page * PLAYER_SLOTS;
        int end = Math.min(start + PLAYER_SLOTS, allPlayers.size());
        for (int i = start; i < end; i++) {
            Player target = allPlayers.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName(target.getName());
            head.setItemMeta(meta);
            inv.setItem(i - start, head);
        }
        if (page > 0) inv.setItem(PREV_SLOT, makeItem(Material.ARROW, "§a上一页", "§7第" + page + "页"));
        inv.setItem(PAGE_SLOT, makeItem(Material.PAPER, "§e第 " + (page + 1) + " / " + totalPages + " 页", null));
        if (page < totalPages - 1) inv.setItem(NEXT_SLOT, makeItem(Material.ARROW, "§a下一页", "§7第" + (page + 2) + "页"));
        // 返回按钮
        if (backTarget != null) inv.setItem(BACK_SLOT, makeItem(Material.BARRIER, "§c返回", null));
        // 存储返回目标
        if (backTarget != null) GUIManager.backTargets.put(player.getUniqueId(), backTarget);

        player.openInventory(inv);
    }

    /**
     * 处理翻页点击，返回被点击的玩家（null=翻页按钮）
     */
    public static Player handleClick(Player clicker, ItemStack item, String rawTitle,
                                      List<Player> allPlayers, int[] pageHolder) {
        if (item.getType() == Material.ARROW) {
            String name = item.getItemMeta().getDisplayName();
            if (name.contains("上一页")) {
                pageHolder[0]--;
            } else if (name.contains("下一页")) {
                pageHolder[0]++;
            }
            open(clicker, allPlayers, getBaseTitle(rawTitle), pageHolder[0], GUIManager.backTargets.getOrDefault(clicker.getUniqueId(), null));
            return null;
        }
        if (item.getType() == Material.PAPER) return null; // 页码指示器
        if (item.getType() != Material.PLAYER_HEAD) return null;
        String name = item.getItemMeta().getDisplayName();
        return Bukkit.getPlayer(name);
    }

    /** 从完整title中提取基础标题（去掉页码部分） */
    public static String getBaseTitle(String fullTitle) {
        int idx = fullTitle.indexOf(" - 第");
        return idx > 0 ? fullTitle.substring(0, idx) : fullTitle;
    }

    public static boolean isPlayerSelectGUI(String title) {
        return title.contains("第") && title.contains("页") && title.contains(" - ");
    }

    private static ItemStack makeItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(Collections.singletonList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
