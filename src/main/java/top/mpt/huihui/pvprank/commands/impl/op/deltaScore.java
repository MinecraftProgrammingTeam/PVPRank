package top.mpt.huihui.pvprank.commands.impl.op;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import static top.mpt.huihui.pvprank.PVPRank.normal;

public class deltaScore extends ICommand {
    public deltaScore() {
        super("deltascore", "", "/pvprank deltascore <团队ID/玩家名> <增减值> --OP指令，增减分数");
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            PlayerUtils.send(sender, normal + "#RED#格式错误！/pvprank deltaScore <团队ID/玩家名> <增减值>");
            return true;
        }
        int delta;
        try { delta = Integer.parseInt(args[1]); }
        catch (NumberFormatException e) {
            PlayerUtils.send(sender, normal + "#RED#分数必须为数字！"); return true;
        }
        try {
            int teamId = Integer.parseInt(args[0]);
            if (TeamExecutor.getTeamData(teamId) != null) {
                TeamExecutor.deltaTeamScore(teamId, delta);
                PlayerUtils.send(sender, normal + "#GREEN#团队 %d 分数已调整 %+d", teamId, delta);
                return true;
            }
        } catch (NumberFormatException ignored) {}
        Player target = Bukkit.getPlayer(args[0]);
        if (target != null && target.isOnline()) {
            TeamExecutor.deltaPlayerScore(target, delta);
            PlayerUtils.send(sender, normal + "#GREEN#玩家 %s 分数已调整 %+d", target.getName(), delta);
            PlayerUtils.send(target, normal + "#GREEN#管理员已将你的分数调整 %+d", delta);
            return true;
        }
        PlayerUtils.send(sender, normal + "#RED#找不到该团队或在线玩家！");
        return true;
    }

    @Override
    public String permission() { return "pvprank.operator"; }
}
