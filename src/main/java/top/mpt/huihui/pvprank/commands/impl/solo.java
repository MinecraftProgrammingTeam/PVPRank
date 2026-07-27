package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static top.mpt.huihui.pvprank.PVPRank.Online_Players;
import static top.mpt.huihui.pvprank.PVPRank.normal;

/**
 * 单挑邀请指令：输入 /pvprank solo <玩家ID> 后，向对方发送可点击接受的邀请
 */
public class solo extends ICommand {

    public static final Map<UUID, UUID> soloInvitations = new ConcurrentHashMap<>();

    public solo() {
        super("solo", "", "/pvprank solo <目标玩家ID> --发起单挑");
        setListParams(Online_Players);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            PlayerUtils.send(sender, "#RED#该指令只能由玩家执行！");
            return true;
        }
        if (args.length != 1) {
            PlayerUtils.send(sender, "#RED#格式错误！正确格式: /pvprank solo <玩家名>");
            return true;
        }
        Player inviter = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            PlayerUtils.send(sender, "#RED#玩家 %s 不在线！", args[0]);
            return true;
        }
        if (inviter.equals(target)) {
            PlayerUtils.send(sender, "#RED#你不能和自己单挑！");
            return true;
        }

        // 存储solo邀请
        soloInvitations.put(target.getUniqueId(), inviter.getUniqueId());

        // 发送可点击tellraw消息
        String msg = normal + "#AQUA#" + inviter.getName() + " #GREEN#邀请你进行单挑！";
        String json = "[{\"text\":\"" + msg.replace("#AQUA#", "").replace("#GREEN#", "")
                .replace(normal, "") + "\",\"color\":\"aqua\"}," +
                "{\"text\":\" [点击接受]\",\"color\":\"gold\"," +
                "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/pvprank accept\"}}]";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + target.getName() + " " + json);

        PlayerUtils.send(inviter, normal + "#GREEN#已向 %s 发送单挑邀请！", target.getName());
        return true;
    }

    @Override
    public String permission() {
        return "pvprank.player";
    }
}
