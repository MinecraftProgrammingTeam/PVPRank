package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.command.CommandSender;
import top.mpt.huihui.pvprank.commands.ICommand;

import static top.mpt.huihui.pvprank.PVPRank.Online_Players;

public class solo extends ICommand {

    public solo() {
        super("solo", "", "/pvprank solo <目标玩家ID>");
        setListParams(Online_Players);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public String permission() {
        return "pvprank.player";
    }
}
