package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;

import static top.mpt.huihui.pvprank.PVPRank.normal;

public class kick extends ICommand {
    public kick() {
        super("kick", "", "/pvprank kick <玩家ID> --从团队种T出玩家");
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            PlayerUtils.send(sender, "#RED#该指令只能由玩家执行！");
            return true;
        }
        if (args.length < 1) {
            PlayerUtils.send(sender, normal + "#RED#格式错误！/pvprank kick <玩家ID>");
            return true;
        }
        Player player = (Player) sender;
        Integer senderTeam = TeamExecutor.getPlayerNormalTeamId(player);
        if (senderTeam == null) {
            PlayerUtils.send(sender, normal + "#RED#你不在任何团队中！");
            return true;
        }
        if (!TeamExecutor.isTeamOwner(player) && !isOperator(player)) {
            PlayerUtils.send(sender, normal + "#RED#你没有权限踢人！只有owner和operator可以。");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            PlayerUtils.send(sender, normal + "#RED#玩家 %s 不在线！", args[0]);
            return true;
        }
        if (target.equals(player)) {
            PlayerUtils.send(sender, normal + "#RED#你不能踢自己！");
            return true;
        }
        Integer targetTeam = TeamExecutor.getPlayerNormalTeamId(target);
        if (targetTeam == null || !targetTeam.equals(senderTeam)) {
            PlayerUtils.send(sender, normal + "#RED#该玩家不在你的团队中！");
            return true;
        }
        PlayerData targetData = TeamExecutor.getPlayerData(target.getUniqueId());
        String targetPerm = targetData != null ? targetData.getPermission() : "member";
        if (TeamExecutor.isTeamOwner(player)) {
            if ("owner".equals(targetPerm)) {
                PlayerUtils.send(sender, normal + "#RED#你不能踢出团队所有者！");
                return true;
            }
        } else {
            if (!"member".equals(targetPerm)) {
                PlayerUtils.send(sender, normal + "#RED#operator只能踢出member！");
                return true;
            }
        }
        TeamExecutor.removePlayer(target);
        PlayerUtils.send(player, normal + "#GREEN#已将 %s 踢出团队！", target.getName());
        PlayerUtils.send(target, normal + "#RED#你已被 %s 踢出团队！", player.getName());
        return true;
    }

    private boolean isOperator(Player player) {
        PlayerData data = TeamExecutor.getPlayerData(player.getUniqueId());
        return data != null && "operator".equals(data.getPermission());
    }

    @Override
    public String permission() { return "pvprank.player"; }
}
