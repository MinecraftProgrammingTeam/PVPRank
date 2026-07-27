package top.mpt.huihui.pvprank.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import top.mpt.huihui.pvprank.executor.TeamExecutor;

import static top.mpt.huihui.pvprank.PVPRank.*;

public class onPlayerJoinAndQuit implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if (!Online_Players.contains(player.getName())) {
            Online_Players.add(player.getName());
        }
        // 如果玩家没注册数据库，注册一下。
        TeamExecutor.checkPlayerExistsAsync(player, exists -> {
            if (!exists) {
                TeamExecutor.registerPlayer(player);
            }

        });

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        Online_Players.remove(player.getName());
        // 如果在战斗，默认对面胜利

    }
}
