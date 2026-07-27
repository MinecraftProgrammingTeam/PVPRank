package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static top.mpt.huihui.pvprank.PVPRank.Online_Players;
import static top.mpt.huihui.pvprank.PVPRank.normal;

/**
 * 组队邀请指令：仅限团队owner邀请玩家加入队伍
 */
public class invite extends ICommand {

    public static final Map<UUID, String[]> teamInvitations = new ConcurrentHashMap<>();

    public invite() {
        super("invite", "", "/pvprank invite <玩家名> --邀请玩家加入团队");
        setListParams(Online_Players);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            PlayerUtils.send(sender, "#RED#该指令只能由玩家执行！");
            return true;
        }
        if (args.length < 1) {
            PlayerUtils.send(sender, normal + "#RED#格式错误！/pvprank invite <玩家名>");
            return true;
        }
        Player inviter = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            PlayerUtils.send(sender, "#RED#玩家 %s 不在线！", args[0]);
            return true;
        }
        if (inviter.equals(target)) {
            PlayerUtils.send(sender, "#RED#你不能邀请自己！");
            return true;
        }

        // 检查邀请者是否在团队中且是owner
        Integer teamId = TeamExecutor.getPlayerNormalTeamId(inviter);
        if (teamId == null) {
            PlayerUtils.send(sender, normal + "#RED#你不在任何团队中！请先创建或加入团队。");
            return true;
        }
        if (!TeamExecutor.isTeamOwner(inviter)) {
            PlayerUtils.send(sender, normal + "#RED#只有团队所有者才能邀请玩家入队！");
            return true;
        }

        // 存储团队邀请
        teamInvitations.put(target.getUniqueId(), new String[]{
                inviter.getUniqueId().toString(), String.valueOf(teamId)
        });

        // 发送可点击tellraw消息
        String msg = normal + "#AQUA#" + inviter.getName() + " #GREEN#邀请你加入团队 #AQUA#" + teamId;
        String cleanMsg = msg.replace("#AQUA#", "").replace("#GREEN#", "").replace(normal, "");
        String json = "[{\"text\":\"" + cleanMsg + "\",\"color\":\"green\"}," +
                "{\"text\":\" [点击接受]\",\"color\":\"gold\"," +
                "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/pvprank accept\"}}]";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + target.getName() + " " + json);

        PlayerUtils.send(inviter, normal + "#GREEN#已向 %s 发送入队邀请！", target.getName());
        return true;
    }

    @Override
    public String permission() {
        return "pvprank.player";
    }
}
