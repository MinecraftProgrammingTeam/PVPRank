package top.mpt.huihui.pvprank.commands.impl.op;

import org.bukkit.command.CommandSender;
import top.mpt.huihui.pvprank.commands.ICommand;

public class deltaScore extends ICommand {
    public deltaScore() {
        super("deltaScore", "", "/pvprank setScore <团队ID> <目标分数(增减值)>");
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
