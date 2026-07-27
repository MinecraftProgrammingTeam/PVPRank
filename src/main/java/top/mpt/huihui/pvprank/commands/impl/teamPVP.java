package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.manager.Team;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static top.mpt.huihui.pvprank.PVPRank.normal;

public class teamPVP extends ICommand {

    public static final Map<UUID, int[]> teamPvpInvitations = new ConcurrentHashMap<>();

    public teamPVP() {
        super("teampvp", "", "/pvprank teampvp <团队编号1> <团队编号2> --发起团队PVP");
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            PlayerUtils.send(sender, "#RED#该指令只能由玩家执行！");
            return true;
        }
        if (args.length < 2) {
            PlayerUtils.send(sender, normal + "#RED#格式错误！/pvprank teamPVP <团队编号1> <团队编号2>");
            return true;
        }
        Player player = (Player) sender;
        if (!TeamExecutor.isTeamOwner(player)) {
            PlayerUtils.send(sender, normal + "#RED#只有团队所有者才能发起团队战！");
            return true;
        }
        int teamAId, teamBId;
        try { teamAId = Integer.parseInt(args[0]); teamBId = Integer.parseInt(args[1]); }
        catch (NumberFormatException e) {
            PlayerUtils.send(sender, normal + "#RED#团队编号必须为数字！"); return true;
        }
        if (teamAId == teamBId) {
            PlayerUtils.send(sender, normal + "#RED#不能自己打自己！"); return true;
        }
        Integer playerTeam = TeamExecutor.getPlayerNormalTeamId(player);
        if (playerTeam == null || playerTeam != teamAId) {
            PlayerUtils.send(sender, normal + "#RED#你必须在团队 %d 中才能发起！", teamAId);
            return true;
        }
        Team teamA = TeamExecutor.getTeamData(teamAId);
        Team teamB = TeamExecutor.getTeamData(teamBId);
        if (teamA == null || teamB == null) {
            PlayerUtils.send(sender, normal + "#RED#指定的队伍不存在！"); return true;
        }
        if (teamA.isInBattle() || teamB.isInBattle()) {
            PlayerUtils.send(sender, normal + "#RED#有一方队伍已在战斗中！"); return true;
        }
        // 找B队在线owner
        Player teamBOwner = null;
        for (PlayerData member : TeamExecutor.getTeamMembers(teamBId)) {
            if ("owner".equals(member.getPermission())) {
                Player p = Bukkit.getPlayer(UUID.fromString(member.getUuid()));
                if (p != null && p.isOnline()) { teamBOwner = p; break; }
            }
        }
        if (teamBOwner == null) {
            PlayerUtils.send(sender, normal + "#RED#对方团队 %d 的队长不在线，无法发起邀请！", teamBId);
            return true;
        }
        teamPvpInvitations.put(teamBOwner.getUniqueId(), new int[]{teamAId, teamBId});
        String cleanMsg = normal + player.getName() + " (团队" + teamA.getName()
                + ") 向你的团队 " + teamB.getName() + " 发起团队战！";
        String json = "[{\"text\":\"" + cleanMsg + "\",\"color\":\"green\"},"
                + "{\"text\":\" [点击接受]\",\"color\":\"gold\","
                + "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/pvprank accept\"}}]";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + teamBOwner.getName() + " " + json);
        PlayerUtils.send(player, normal + "#GREEN#已向团队 %s 的队长 %s 发送团队战邀请！",
                teamB.getName(), teamBOwner.getName());
        return true;
    }

    @Override
    public String permission() { return "pvprank.player"; }
}
