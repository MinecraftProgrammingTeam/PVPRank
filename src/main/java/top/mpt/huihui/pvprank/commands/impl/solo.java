package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.Team;
import top.mpt.huihui.pvprank.utils.ConfigUtils;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.util.Collections;
import java.util.List;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;
import static top.mpt.huihui.pvprank.PVPRank.Online_Players;
import static top.mpt.huihui.pvprank.PVPRank.instance;

public class solo extends ICommand {

    public solo() {
        super("solo", "", "/pvprank solo <目标玩家ID>");
        setListParams(Online_Players);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 1) {
            PlayerUtils.send(sender, "#RED#格式错误！正确格式为:/pvprank solo <玩家名>");
            return true;
        }
        if (sender instanceof Player){
            // 设置玩家状态
            Player playerA = (Player) sender;
            Player playerB = Bukkit.getPlayer(args[0]);
            // 如果正在solo就取消
            if (TeamExecutor.isPlayerInSoloPvP(playerA) || TeamExecutor.isPlayerInSoloPvP(playerB)
            || TeamExecutor.isPlayerInTeamPvP(playerA) || TeamExecutor.isPlayerInTeamPvP(playerB)
            ) {
                PlayerUtils.send(sender, "#RED#您或者对方正在solo中或团队竞技中，请等待比赛结束再发出邀请。");
                return true;
            }
            // 添加玩家（自动入队）
            TeamExecutor.addSingleTeam(playerA);
            TeamExecutor.addSingleTeam(playerB);
            // 设置对手
            TeamExecutor.setPlayerOpponent(playerA.getUniqueId().toString(), playerB.getUniqueId().toString());
            TeamExecutor.setPlayerOpponent(playerB.getUniqueId().toString(), playerA.getUniqueId().toString());
            // 利用MV插件传送玩家
            // mvtp X_huihui e:survival:100,64,200
            List<?> templist = getWolrds(true);
            Collections.shuffle(templist);
            String worldName = (String) templist.get(0);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "mv tp " + playerA.getName() + " e:" + worldName +
                            ":" + getLocations(worldName).get(0));

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "mv tp " + playerB.getName() + " e:" + worldName +
                            ":" + getLocations(worldName).get(1));

        }
        return true;
    }

    @Override
    public String permission() {
        return "pvprank.player";
    }

    private List<?> getWolrds(boolean isSolo){
        if (isSolo){
            return ConfigUtils.getListConfig(instance.getConfig(), "worlds_for_solo");
        }
        return ConfigUtils.getListConfig(instance.getConfig(), "worlds_for_team");
    }



    private List<?> getLocations(String singleWolrd){
        return ConfigUtils.getListConfig(instance.getConfig(), "worlds_for_solo." + singleWolrd);
    }
}
