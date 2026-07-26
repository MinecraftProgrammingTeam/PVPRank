package top.mpt.huihui.pvprank.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.utils.ConfigUtils;


import java.util.*;

import static top.mpt.huihui.pvprank.PVPRank.instance;
import static top.mpt.huihui.pvprank.PVPRank.modeStarted;

public class onPlayerDeath implements Listener {
    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event){
        modeStarted = (boolean) ConfigUtils.getDefaultConfig("start", false);
        if (!modeStarted){
            return;
        }
        Player playerA = event.getEntity();
        PlayerData playerDataA = TeamExecutor.getPlayerData(playerA.getUniqueId());


        // 如果玩家在单挑
        if (TeamExecutor.isPlayerInSoloPvP(playerA)){
            // 对手
            Player playerB = Bukkit.getPlayer(playerDataA.getOpponentUuid());
            // 清空背包
            playerA.getInventory().clear();
            playerB.getInventory().clear();
            // 如果玩家在单人PVP临时团队里面
            if (TeamExecutor.isPlayerInSoloTeam(playerA)){
                // 解散
                TeamExecutor.removePlayer(playerA);
            }

            // 如果对手在单人PVP临时队伍里面
            if (TeamExecutor.isPlayerInSoloTeam(playerB)){
                TeamExecutor.removePlayer(playerB);
            }

            // 给玩家加分和扣分
            TeamExecutor.deltaPlayerScore(playerA, -20);
            TeamExecutor.deltaPlayerScore(playerB, 20);

            // 传送玩家回到出生点
            Bukkit.dispatchCommand(playerB, "home");

        }

    }


}
