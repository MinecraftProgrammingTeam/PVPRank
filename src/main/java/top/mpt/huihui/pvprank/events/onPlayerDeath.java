package top.mpt.huihui.pvprank.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import top.mpt.huihui.pvprank.executor.PvPManager;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.utils.ConfigUtils;
import top.mpt.huihui.pvprank.utils.LogUtils;

import java.util.UUID;

import static top.mpt.huihui.pvprank.PVPRank.modeStarted;

public class onPlayerDeath implements Listener {
    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event) {
        modeStarted = (boolean) ConfigUtils.getDefaultConfig("start", false);
        if (!modeStarted) return;

        Player dead = event.getEntity();
        PlayerData deadData = TeamExecutor.getPlayerData(dead.getUniqueId());
        if (deadData == null) return;

        // 处理单人PVP死亡
        if (TeamExecutor.isPlayerInSoloPvP(dead)) {
            String opponentUuid = deadData.getOpponentUuid();
            if (opponentUuid != null) {
                Player winner = Bukkit.getPlayer(UUID.fromString(opponentUuid));
                if (winner != null && winner.isOnline()) {
                    event.getDrops().clear();
                    PvPManager.endSoloPvP(winner, dead);
                }
            }
            return;
        }

        // 处理团队PVP死亡
        if (TeamExecutor.isPlayerInTeamPvP(dead)) {
            event.getDrops().clear();
            PvPManager.handleTeamPvPDeath(dead);
        }
    }
}
