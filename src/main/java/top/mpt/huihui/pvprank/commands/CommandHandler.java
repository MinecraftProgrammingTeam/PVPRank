package top.mpt.huihui.pvprank.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import top.mpt.huihui.pvprank.commands.impl.*;
import top.mpt.huihui.pvprank.commands.impl.op.addPlayer;
import top.mpt.huihui.pvprank.commands.impl.op.deltaScore;
import top.mpt.huihui.pvprank.commands.impl.op.forceKick;
import top.mpt.huihui.pvprank.commands.impl.op.status;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.util.*;

import static top.mpt.huihui.pvprank.PVPRank.Online_Players;

public class CommandHandler implements TabExecutor {

    private final Map<String, ICommand> commands = new HashMap<>();

    public CommandHandler() { initHandler(); }

    private void initHandler() {
        registerCommand(new accept());
        registerCommand(new breakup());
        registerCommand(new createTeam());
        registerCommand(new invite());
        registerCommand(new kick());
        registerCommand(new setPermission());
        registerCommand(new solo());
        registerCommand(new teamPVP());
        registerCommand(new addPlayer());
        registerCommand(new deltaScore());
        registerCommand(new status());
        registerCommand(new forceKick());
    }

    public void registerCommand(ICommand command) {
        commands.put(command.getCmdName(), command);
    }

    public void showHelp(CommandSender sender) {
        sender.sendMessage("§bPVPRank §a帮助");
        for (String key : commands.keySet()) {
            sender.sendMessage(commands.get(key).showUsage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args == null || args.length < 1) {
            showHelp(sender);
            return true;
        }
        ICommand cmd = commands.get(args[0].toLowerCase());
        try {
            if (cmd != null) {
                if (!sender.hasPermission(cmd.permission())) {
                    PlayerUtils.send(sender, "#RED#你没有权限执行该指令！");
                    return true;
                }
                String[] params = new String[0];
                if (args.length >= 2) {
                    LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
                    list.removeFirst();
                    params = list.toArray(new String[0]);
                }
                boolean res = cmd.onCommand(sender, params);
                if (!res) {
                    sender.sendMessage(cmd.showUsage());
                }
            } else {
                showHelp(sender);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage("§c[PVPRank] 异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args == null || args.length < 1) {
            return null;
        }
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            String typingStr = args[0].toLowerCase();
            for (String cmdName : commands.keySet()) {
                if (cmdName.startsWith(typingStr)) {
                    ICommand cmd = commands.get(cmdName);
                    if (sender.hasPermission(cmd.permission())) {
                        result.add(cmdName);
                    }
                }
            }
            return result;
        }
        ICommand cmd = commands.get(args[0].toLowerCase());
        if (cmd == null || !sender.hasPermission(cmd.permission())) {
            return result;
        }
        String cmdName = cmd.getCmdName();
        boolean needsPlayer = cmdName.equals("solo") || cmdName.equals("invite")
                || cmdName.equals("kick") || cmdName.equals("setpermission")
                || cmdName.equals("addplayer") || cmdName.equals("forcekick");
        if (args.length == 2) {
            if (needsPlayer) {
                return filterByPrefix(Online_Players, args[1]);
            }
            List<String> customParams = cmd.getListParams();
            if (customParams != null && !customParams.isEmpty()) {
                return filterByPrefix(customParams, args[1]);
            }
        } else if (args.length == 3) {
            if (cmdName.equals("setpermission") || cmdName.equals("addplayer")) {
                return filterByPrefix(cmd.getListParams(), args[2]);
            }
        }
        return result;
    }

    private List<String> filterByPrefix(List<String> source, String prefix) {
        List<String> filtered = new ArrayList<>();
        String lower = prefix.toLowerCase();
        for (String s : source) {
            if (s.toLowerCase().startsWith(lower)) {
                filtered.add(s);
            }
        }
        return filtered;
    }
}
