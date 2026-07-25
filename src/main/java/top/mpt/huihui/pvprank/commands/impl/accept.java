package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.command.CommandSender;
import top.mpt.huihui.pvprank.commands.ICommand;

public class accept extends ICommand {
    public accept() {
        super("accept", "", "/pvprank accept");
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
