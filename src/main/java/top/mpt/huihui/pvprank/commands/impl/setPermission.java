package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.command.CommandSender;
import top.mpt.huihui.pvprank.commands.ICommand;

import java.util.ArrayList;

public class setPermission extends ICommand {
    public setPermission() {
        super("setPermission", "", "/pvprank serPermission <玩家ID> <权限>");
        ArrayList<String> listParams = new ArrayList<>();
        listParams.add("owner");
        listParams.add("operator");
        listParams.add("member");
        setListParams(listParams);
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
