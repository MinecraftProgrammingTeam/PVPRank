package top.mpt.huihui.pvprank.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.utils.ConfigUtils;


import static top.mpt.huihui.pvprank.PVPRank.instance;
import static top.mpt.huihui.pvprank.PVPRank.modeStarted;
import static top.mpt.huihui.pvprank.executor.TeamExecutor.SOLO_MAX;
import static top.mpt.huihui.pvprank.executor.TeamExecutor.SOLO_MIN;

public class onPlayerDeath implements Listener {
    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event){
        modeStarted = (boolean) ConfigUtils.getDefaultConfig("start", false);
        if (!modeStarted){
            return;
        }
        Player player = event.getEntity();
        PlayerData playerData = TeamExecutor.getPlayerData(player.getUniqueId());
        Integer currentTeamId = (playerData != null) ? playerData.getTeamId() : null;

        // 如果玩家在单挑
        if (TeamExecutor.isPlayerInSoloPvP(player)){
            // 如果玩家在单人PVP临时团队里面
            if (TeamExecutor.isPlayerInSoloTeam(player)){
                // 解散
                TeamExecutor.removePlayer(player);
            }

            // 给玩家加分和扣分
        }

    }
}
