package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import static top.mpt.huihui.pvprank.PVPRank.Online_Players;

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
            TeamExecutor.addSingleTeam(playerA);
            TeamExecutor.addSingleTeam(playerB);
            // 利用MV插件传送玩家

        }
        return true;
    }

    @Override
    public String permission() {
        return "pvprank.player";
    }
}
