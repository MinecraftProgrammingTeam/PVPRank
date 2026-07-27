package top.mpt.huihui.pvprank.commands.impl.op;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.manager.Team;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.util.List;
import java.util.UUID;

/**
 * OP状态查询指令——以表格形式展示数据库中的玩家和团队数据
 *
 * 用法：
 *   /pvprank status <玩家ID>    — 查单个玩家
 *   /pvprank status <群组ID>    — 查单个团队
 *   /pvprank status Player      — 查全部玩家
 *   /pvprank status Team        — 查全部团队
 *   /pvprank status             — 查全部
 */
public class status extends ICommand {

    public status() {
        super("status", "", "/pvprank status [玩家ID/群组ID/Player/Team]");
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        String mode = (args.length > 0) ? args[0] : "all";

        // 尝试解析为玩家名——查单个玩家
        PlayerData pd = TeamExecutor.getPlayerDataFromName(mode);
        if (pd != null) {
            showPlayerTable(sender, pd);
            return true;
        }

        // 尝试解析为团队ID——查单个团队
        try {
            int tid = Integer.parseInt(mode);
            Team t = TeamExecutor.getTeamData(tid);
            if (t != null) {
                showTeamTable(sender, t);
                // 同时显示该团队成员
                List<PlayerData> members = TeamExecutor.getTeamMembers(tid);
                if (members != null && !members.isEmpty()) {
                    showPlayerTable(sender, members, "团队 " + tid + " 成员");
                }
                return true;
            }
        } catch (NumberFormatException ignored) {}

        // 关键字模式
        if (mode.equalsIgnoreCase("Player")) {
            showPlayerTable(sender, TeamExecutor.getAllPlayers(), "全部玩家");
            return true;
        }
        if (mode.equalsIgnoreCase("Team")) {
            showTeamTable(sender, TeamExecutor.getAllTeams());
            return true;
        }

        // 默认：显示全部
        showPlayerTable(sender, TeamExecutor.getAllPlayers(), "全部玩家");
        showTeamTable(sender, TeamExecutor.getAllTeams());
        return true;
    }

    // ========== 表格渲染 ==========

    private void showPlayerTable(CommandSender sender, PlayerData pd) {
        sender.sendMessage("§b========== 玩家数据 ==========");
        sender.sendMessage("§eUUID: §f" + pd.getUuid());
        sender.sendMessage("§e名称: §f" + pd.getPlayerName());
        sender.sendMessage("§e团队: §f" + (pd.getTeamId() != null ? pd.getTeamId() : "无"));
        sender.sendMessage("§e积分: §f" + pd.getPersonalScore());
        sender.sendMessage("§e战斗中: §f" + (pd.isInBattle() ? "§c是" : "§a否"));
        sender.sendMessage("§e权限: §f" + pd.getPermission());
        sender.sendMessage("§e对手: §f" + (pd.getOpponentUuid() != null ? Bukkit.getPlayer(UUID.fromString(pd.getOpponentUuid())).getName() : "无"));
        sender.sendMessage("§b==============================");
    }

    private void showPlayerTable(CommandSender sender, List<PlayerData> players, String title) {
        if (players == null || players.isEmpty()) {
            sender.sendMessage("§e无玩家数据。");
            return;
        }
        sender.sendMessage("§b===== " + title + " (" + players.size() + "人) =====");
        sender.sendMessage("§a名称  |  团队  |   积分   | 战斗 |  权限   | 对手");
        sender.sendMessage("§7----------|------|--------|----|-------|-----");
        for (PlayerData p : players) {
            sender.sendMessage("§f" + padRight(p.getPlayerName(), 12)
                    + "| " + padRight(p.getTeamId() != null ? String.valueOf(p.getTeamId()) : "-", 5)
                    + "| " + padRight(String.valueOf(p.getPersonalScore()), 7)
                    + "| " + padRight(p.isInBattle() ? "是" : "否", 2)
                    + "| " + padRight(p.getPermission(), 7)
                    + "| " + (p.getOpponentUuid() != null ? truncate(Bukkit.getPlayer(UUID.fromString(p.getOpponentUuid())).getName(), 8) : "-"));
        }
        sender.sendMessage("§b==================================");
    }

    private void showTeamTable(CommandSender sender, Team t) {
        sender.sendMessage("§b========== 团队数据 ==========");
        sender.sendMessage("§eID: §f" + t.getId());
        sender.sendMessage("§e名称: §f" + t.getName());
        sender.sendMessage("§e积分: §f" + t.getScore());
        sender.sendMessage("§e战斗中: §f" + (t.isInBattle() ? "§c是" : "§a否"));
        sender.sendMessage("§e对手ID: §f" + (t.getOpponentTeamId() != null ? String.valueOf(t.getOpponentTeamId()) : "无"));
        sender.sendMessage("§b==============================");
    }

    private void showTeamTable(CommandSender sender, List<Team> teams) {
        if (teams == null || teams.isEmpty()) {
            sender.sendMessage("§e无团队数据。");
            return;
        }
        sender.sendMessage("§b===== 全部团队 (" + teams.size() + "个) =====");
        sender.sendMessage("§aID  | 名称          |   积分   | 战斗 | 对手");
        sender.sendMessage("§7----|-------------|--------|----|----");
        for (Team t : teams) {
            sender.sendMessage("§f" + padRight(String.valueOf(t.getId()), 4)
                    + "| " + padRight(t.getName(), 12)
                    + "| " + padRight(String.valueOf(t.getScore()), 7)
                    + "| " + padRight(t.isInBattle() ? "是" : "否", 5)
                    + "| " + (t.getOpponentTeamId() != null ? String.valueOf(t.getOpponentTeamId()) : "-"));
        }
        sender.sendMessage("§b==================================");
    }

    private String padRight(String s, int len) {
        if (s == null) s = "-";
        int visibleLen = s.replaceAll("§.", "").length();
        StringBuilder sb = new StringBuilder(s);
        while (visibleLen < len) { sb.append(" "); visibleLen++; }
        return sb.toString();
    }

    private String truncate(String s, int len) {
        if (s == null) return "-";
        return s.length() > len ? s.substring(0, len) + "..." : s;
    }

    @Override
    public String permission() { return "pvprank.operator"; }
}
