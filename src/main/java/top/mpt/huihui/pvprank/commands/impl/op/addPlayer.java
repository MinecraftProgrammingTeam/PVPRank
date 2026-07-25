package top.mpt.huihui.pvprank.commands.impl.op;

import org.bukkit.command.CommandSender;
import top.mpt.huihui.pvprank.commands.ICommand;

import static top.mpt.huihui.pvprank.PVPRank.Online_Players;

public class addPlayer extends ICommand {
    public addPlayer() {
        super("addPlayer", "", "/pvprank addPlayer <玩家ID> <权限>");
        setListParams(Online_Players);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public String permission() {
        return "pvprank.operator";
    }
}
