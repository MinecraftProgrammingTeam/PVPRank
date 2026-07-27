package top.mpt.huihui.pvprank.commands.impl.op;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import static top.mpt.huihui.pvprank.PVPRank.Online_Players;
import static top.mpt.huihui.pvprank.PVPRank.normal;

public class addPlayer extends ICommand {
    public addPlayer() {
        super("addplayer", "", "/pvprank addplayer <团队ID> <玩家ID> <owner/operator/member> --OP指令，强制入队");
        setListParams(Online_Players);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            PlayerUtils.send(sender, normal + "#RED#格式错误！/pvprank addPlayer <团队ID> <玩家ID> <权限>");
            return true;
        }
        int teamId;
        try { teamId = Integer.parseInt(args[0]); }
        catch (NumberFormatException e) {
            PlayerUtils.send(sender, normal + "#RED#团队编号必须为数字！"); return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            PlayerUtils.send(sender, normal + "#RED#玩家 %s 不在线！", args[1]);
            return true;
        }
        String perm = args[2].toLowerCase();
        if (!perm.equals("owner") && !perm.equals("operator") && !perm.equals("member")) {
            PlayerUtils.send(sender, normal + "#RED#无效的权限！owner, operator, member");
            return true;
        }
        TeamExecutor.addPlayer(target, teamId);
        TeamExecutor.setPlayerPermission(target, perm);
        PlayerUtils.send(sender, normal + "#GREEN#已将 %s 添加到团队 %d，权限: %s", target.getName(), teamId, perm);
        PlayerUtils.send(target, normal + "#GREEN#管理员已将你添加到团队 %d，权限: %s", teamId, perm);
        return true;
    }

    @Override
    public String permission() { return "pvprank.operator"; }
}
