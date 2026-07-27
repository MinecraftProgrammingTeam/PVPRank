package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.PvPManager;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.util.UUID;

import static top.mpt.huihui.pvprank.PVPRank.normal;

public class accept extends ICommand {
    public accept() {
        super("accept", "", "/pvprank accept --接受请求(一般不需要手动输入)");
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            PlayerUtils.send(sender, "#RED#该指令只能由玩家执行！");
            return true;
        }
        Player acceptor = (Player) sender;

        UUID soloInviterUuid = solo.soloInvitations.remove(acceptor.getUniqueId());
        if (soloInviterUuid != null) {
            Player inviter = Bukkit.getPlayer(soloInviterUuid);
            if (inviter == null || !inviter.isOnline()) {
                PlayerUtils.send(sender, normal + "#RED#邀请者已下线！"); return true;
            }
            if (TeamExecutor.isPlayerInSoloPvP(inviter) || TeamExecutor.isPlayerInSoloPvP(acceptor)
                    || TeamExecutor.isPlayerInTeamPvP(inviter) || TeamExecutor.isPlayerInTeamPvP(acceptor)) {
                PlayerUtils.send(sender, normal + "#RED#你或邀请者正在战斗中！"); return true;
            }
            PvPManager.startSoloPvP(inviter, acceptor);
            return true;
        }

        String[] teamData = invite.teamInvitations.remove(acceptor.getUniqueId());
        if (teamData != null) {
            String inviterUuid = teamData[0];
            int teamId = Integer.parseInt(teamData[1]);
            Player inviter = Bukkit.getPlayer(UUID.fromString(inviterUuid));
            if (inviter == null || !inviter.isOnline()) {
                PlayerUtils.send(sender, normal + "#RED#邀请者已下线！"); return true;
            }
            Integer currentTeam = TeamExecutor.getPlayerNormalTeamId(acceptor);
            if (currentTeam != null) {
                PlayerUtils.send(sender, normal + "#RED#请先退出当前队伍再接受邀请！"); return true;
            }
            TeamExecutor.addPlayer(acceptor, teamId);
            PlayerUtils.send(acceptor, normal + "#GREEN#你已加入 %s 的队伍！", inviter.getName());
            if (inviter != null) PlayerUtils.send(inviter, normal + "#GREEN#%s 已接受邀请加入队伍！", acceptor.getName());
            return true;
        }

        // 检查团队PVP邀请
        int[] teamPvpData = teamPVP.teamPvpInvitations.remove(acceptor.getUniqueId());
        if (teamPvpData != null) {
            int teamAId = teamPvpData[0];
            int teamBId = teamPvpData[1];
            // 验证acceptor确实是B队owner
            if (!TeamExecutor.isTeamOwner(acceptor)) {
                PlayerUtils.send(sender, normal + "#RED#只有团队所有者才能接受团队战邀请！");
                return true;
            }
            Integer acceptorTeam = TeamExecutor.getPlayerNormalTeamId(acceptor);
            if (acceptorTeam == null || acceptorTeam != teamBId) {
                PlayerUtils.send(sender, normal + "#RED#你已不是团队 %d 的队长，邀请无效！", teamBId);
                return true;
            }
            PvPManager.startTeamPvP(acceptor, teamAId, teamBId);
            return true;
        }


        PlayerUtils.send(sender, normal + "#RED#你没有待处理的邀请！");
        return true;
    }

    @Override
    public String permission() { return "pvprank.player"; }
}
