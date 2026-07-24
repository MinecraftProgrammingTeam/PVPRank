package top.mpt.huihui.pvprank.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;


import static top.mpt.huihui.pvprank.PVPRank.modeStarted;

public class onPlayerDeath implements Listener {
    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event){
        if (!modeStarted){
            return;
        }


    }
}
