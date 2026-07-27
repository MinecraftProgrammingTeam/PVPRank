package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.util.List;

import static top.mpt.huihui.pvprank.PVPRank.normal;

public class breakup extends ICommand {
    public breakup() {
        super("breakup", "", "/pvprank breakup <团队ID> --解散团队");
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            PlayerUtils.send(sender, "#RED#该指令只能由玩家执行！");
            return true;
        }
        Player player = (Player) sender;

        // 获取玩家所在团队
        Integer teamId = TeamExecutor.getPlayerNormalTeamId(player);
        if (teamId == null) {
            PlayerUtils.send(sender, normal + "#RED#你不在任何团队中！");
            return true;
        }

        // 检查是否是owner
        if (!TeamExecutor.isTeamOwner(player)) {
            PlayerUtils.send(sender, normal + "#RED#只有团队所有者才能解散团队！");
            return true;
        }

        // 检查团队是否在战斗中
        if (TeamExecutor.isPlayerInTeamPvP(player)) {
            PlayerUtils.send(sender, normal + "#RED#团队正在战斗中，无法解散！");
            return true;
        }

        // 获取所有成员并踢出
        List<PlayerData> members = TeamExecutor.getTeamMembers(teamId);
        TeamExecutor.removeTeam(teamId);

        PlayerUtils.send(player, normal + "#GREEN#团队 #AQUA#%d #GREEN#已解散！", teamId);
        return true;
    }

    @Override
    public String permission() {
        return "pvprank.player";
    }
}
