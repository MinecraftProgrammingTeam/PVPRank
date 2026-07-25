package top.mpt.huihui.pvprank.commands.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mpt.huihui.pvprank.commands.ICommand;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.utils.ChatUtils;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import static top.mpt.huihui.pvprank.PVPRank.normal;
import static top.mpt.huihui.pvprank.executor.TeamExecutor.SOLO_MAX;
import static top.mpt.huihui.pvprank.executor.TeamExecutor.SOLO_MIN;

public class createTeam extends ICommand {

    public createTeam() {
        super("addTeam", "", "/pvprank addTeam <团队编号> <团队名称>");
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length != 2){
            PlayerUtils.send(sender, normal + "#RED#指令格式不正确！");
            return true;
        }
        if (!isNumeric(args[0])){
            PlayerUtils.send(sender, normal + "#RED#请确保输入的团队编号为纯数字！");
            return true;
        }
        if (sender instanceof Player){
            Player player = (Player) sender;
            PlayerData playerData = TeamExecutor.getPlayerData(player.getUniqueId());
            Integer currentTeamId = (playerData != null) ? playerData.getTeamId() : null;
            if (currentTeamId != null){
                if (currentTeamId < SOLO_MIN || currentTeamId > SOLO_MAX){
                    PlayerUtils.send(player, normal + "#RED#请先退出当前的%d号团队再创建新团队！", currentTeamId);
                } else {
                    PlayerUtils.send(player, normal + "#RED#请先结束PVP再创建团队");
                }
                return true;
            }
        }
        TeamExecutor.addTeam(Integer.parseInt(args[0]), args[1], sender);
        return true;
    }

    @Override
    public String permission() {
        return "pvprank.player";
    }

    public boolean isNumeric(String str)
    {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
    return true;
    }
}
