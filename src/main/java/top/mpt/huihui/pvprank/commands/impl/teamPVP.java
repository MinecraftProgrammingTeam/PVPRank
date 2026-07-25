package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.command.CommandSender;
import top.mpt.huihui.pvprank.commands.ICommand;

public class teamPVP extends ICommand {
    public teamPVP() {
        super("teamPVP", "", "/pvprank teamPVP <团队编号1> <团队编号2>");
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public String permission() {
        return "";
    }
}
