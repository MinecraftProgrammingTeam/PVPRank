package top.mpt.huihui.pvprank.commands.impl.op;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import static top.mpt.huihui.pvprank.PVPRank.normal;

public class forceKick extends ICommand {
    public forceKick() {
        super("forcekick", "", "/pvprank forcekick <群组ID> <玩家ID> --OP指令，强制T出");
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            PlayerUtils.send(sender, normal + "#RED#格式错误！/pvprank forcekick <群组ID> <玩家ID>");
            return true;
        }
        int teamId;
        try { teamId = Integer.parseInt(args[0]); }
        catch (NumberFormatException e) {
            PlayerUtils.send(sender, normal + "#RED#群组ID必须为数字！");
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            PlayerUtils.send(sender, normal + "#RED#玩家 %s 不在线！", args[1]);
            return true;
        }
        PlayerData targetData = TeamExecutor.getPlayerData(target.getUniqueId());
        Integer targetTeam = targetData != null ? targetData.getTeamId() : null;
        if (targetTeam == null || targetTeam != teamId) {
            PlayerUtils.send(sender, normal + "#RED#该玩家不在群组 %d 中！", teamId);
            return true;
        }
        TeamExecutor.removePlayer(target);
        PlayerUtils.send(sender, normal + "#GREEN#已将 %s 从群组 %d 中踢出！", target.getName(), teamId);
        PlayerUtils.send(target, normal + "#RED#你已被管理员从群组 %d 中踢出！", teamId);
        return true;
    }

    @Override
    public String permission() { return "pvprank.operator"; }
}
