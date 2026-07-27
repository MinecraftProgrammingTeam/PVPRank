package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.util.ArrayList;

import static top.mpt.huihui.pvprank.PVPRank.normal;

public class setPermission extends ICommand {
    public setPermission() {
        super("setpermission", "", "/pvprank setpermission <玩家ID> <owner/operator/member>");
        ArrayList<String> listParams = new ArrayList<>();
        listParams.add("owner");
        listParams.add("operator");
        listParams.add("member");
        setListParams(listParams);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            PlayerUtils.send(sender, "#RED#该指令只能由玩家执行！");
            return true;
        }
        if (args.length < 2) {
            PlayerUtils.send(sender, normal + "#RED#格式错误！正确格式:/pvprank setPermission <玩家ID> <权限>");
            return true;
        }
        Player player = (Player) sender;
        if (!TeamExecutor.isTeamOwner(player)) {
            PlayerUtils.send(sender, normal + "#RED#只有团队所有者才能修改成员权限！");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            PlayerUtils.send(sender, normal + "#RED#玩家 %s 不在线！", args[0]);
            return true;
        }
        String perm = args[1].toLowerCase();
        if (!perm.equals("owner") && !perm.equals("operator") && !perm.equals("member")) {
            PlayerUtils.send(sender, normal + "#RED#无效的权限！请使用: owner, operator, member");
            return true;
        }
        Integer ownerTeam = TeamExecutor.getPlayerNormalTeamId(player);
        Integer targetTeam = TeamExecutor.getPlayerNormalTeamId(target);
        if (targetTeam == null || !targetTeam.equals(ownerTeam)) {
            PlayerUtils.send(sender, normal + "#RED#该玩家不在你的团队中！");
            return true;
        }
        if (player.equals(target)) {
            PlayerUtils.send(sender, normal + "#RED#你不能修改自己的权限！");
            return true;
        }
        TeamExecutor.setPlayerPermission(target, perm);
        PlayerUtils.send(player, normal + "#GREEN#已将 %s 的权限设置为 %s", target.getName(), perm);
        PlayerUtils.send(target, normal + "#GREEN#你的权限已被 %s 设置为 %s", player.getName(), perm);
        return true;
    }

    @Override
    public String permission() {
        return "pvprank.player";
    }
}
