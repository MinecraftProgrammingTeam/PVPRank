package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.command.CommandSender;
import top.mpt.huihui.pvprank.commands.ICommand;

public class breakup extends ICommand {
    public breakup() {
        super("breakup", "", "/pvprank breakup <团队ID>");
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
