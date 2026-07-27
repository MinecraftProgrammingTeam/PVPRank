package top.mpt.huihui.pvprank.executor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import top.mpt.huihui.pvprank.manager.DAO;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.manager.Team;
import top.mpt.huihui.pvprank.utils.ChatUtils;
import top.mpt.huihui.pvprank.utils.LogUtils;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static top.mpt.huihui.pvprank.PVPRank.*;

public class TeamExecutor {
    private static final DAO dao = teamPlayerDAO;
    private static final JavaPlugin plugin = instance;

    public static void addTeam(int teamID, String teamName, CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String finalName = teamName;
            if (Objects.equals(teamName, "")) {
                finalName = "Team-" + teamID;
            }
            if (teamID <= 0 || teamID == Integer.MAX_VALUE) {
                PlayerUtils.send(sender, normal + "#RED#" + "您输入的数字不合规，请输入区间(0,2147483647)内的数字");
            } else if (dao.getTeam(teamID) != null) {
                PlayerUtils.send(sender, normal + "#RED#" + "该队伍ID已经存在！");
            } else {
                dao.saveTeam(teamID, finalName, 0, false, null);
                ChatUtils.broadcast("#GREEN#" + "PVP团队: #AQUA#%s #GREEN#已被 #AQUA#%s #GREEN#创建。团队编号为：%d", teamName, sender.getName(), teamID);
                LogUtils.info("创建队伍成功，ID: " + teamID + ", " + "名称: " + finalName);
                if (sender instanceof Player) {
                    addPlayer((Player) sender, teamID);
                    setPlayerPermission((Player) sender, "owner");
                }
            }
        });
    }

    public static void registerPlayer(Player player) {
        dao.savePlayer(
                String.valueOf(player.getUniqueId()),
                player.getName(),
                null,
                0,
                false,
                Instant.now().toEpochMilli(),
                "member",
                null);
    }

    public static void addPlayer(Player player, int teamID) {
        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerData playerData = dao.getPlayer(uuid.toString());
            Integer currentTeamId = (playerData != null) ? playerData.getTeamId() : null;
            if (currentTeamId != null) {
                PlayerUtils.send(player, normal + "#RED#检测到您仍然在队伍：%d中，请退出后再加入新的队伍。", currentTeamId);
            } else {
                dao.setPlayerTeam(uuid.toString(), teamID);
                dao.setPlayerInBattle(String.valueOf(uuid), false);
                LogUtils.info(player.getName() + " 加入团队 " + teamID);
            }
        });
    }

    public static void removeTeam(int teamID) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            dao.getPlayersByTeam(teamID).forEach((player) -> {
                dao.setPlayerPermission(player.getUuid(),  "member");
            });
            dao.deleteTeam(teamID);
            LogUtils.info("队伍 " + teamID + " 已解散");
        });
    }

    public static void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            dao.setPlayerTeam(uuid.toString(), null);
            dao.setPlayerInBattle(String.valueOf(uuid), false);
        });
    }

    public static boolean isPlayerInSoloPvP(Player player) {
        PlayerData playerData = dao.getPlayer(player.getUniqueId().toString());
        return playerData.isInBattle();
    }

    public static boolean isPlayerInTeamPvP(Player player) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        Integer currentTeamId = (playerData != null) ? playerData.getTeamId() : null;
        if (currentTeamId != null) {
            Team team = dao.getTeam(currentTeamId);
            return team != null && team.isInBattle();
        }
        return false;
    }

    public static void checkPlayerExistsAsync(Player player, java.util.function.Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean exists = dao.existsPlayer(player.getUniqueId().toString());
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(exists));
        });
    }

    public static void setPlayerOpponent(String playerUUID, String opponentUUID) {
        dao.setPlayerOpponent(playerUUID, opponentUUID);
    }

    public static void setPlayerInBattle(String playerUUID, boolean status) {
        dao.setPlayerInBattle(playerUUID, status);
    }

    public static void setPlayerPermission(Player player, String permission) {
        dao.setPlayerPermission(player.getUniqueId().toString(), permission);
    }

    public static void deltaPlayerScore(Player player, int score) {
        dao.addPlayerScore(player.getUniqueId().toString(), score);
    }

    public static PlayerData getPlayerData(UUID uuid) {
        return dao.getPlayer(uuid.toString());
    }

    public static Team getTeamData(int teamId) {
        return dao.getTeam(teamId);
    }

    public static List<PlayerData> getTeamMembers(int teamId) {
        return dao.getPlayersByTeam(teamId);
    }

    public static void setTeamInBattle(int teamId, boolean inBattle) {
        dao.setTeamInBattle(teamId, inBattle);
    }

    public static void setTeamOpponent(int teamId, Integer opponentTeamId) {
        dao.setTeamOpponent(teamId, opponentTeamId);
    }

    public static void deltaTeamScore(int teamId, int score) {
        dao.addTeamScore(teamId, score);
    }

    public static boolean isTeamOwner(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        return data != null && "owner".equals(data.getPermission());
    }

    public static Integer getPlayerNormalTeamId(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        return (data != null) ? data.getTeamId() : null;
    }

    public static boolean isPlayerInNormalTeam(Player player) {
        return getPlayerNormalTeamId(player) != null;
    }

    public static List<Player> getOnlineTeamMembers(int teamId) {
        List<Player> onlineMembers = new ArrayList<>();
        List<PlayerData> members = dao.getPlayersByTeam(teamId);
        for (PlayerData member : members) {
            Player p = Bukkit.getPlayer(UUID.fromString(member.getUuid()));
            if (p != null && p.isOnline()) onlineMembers.add(p);
        }
        return onlineMembers;
    }

    public static List<PlayerData> getAllPlayers() {
        return dao.getAllPlayers();
    }

    public static List<Team> getAllTeams() {
        return dao.getAllTeams();
    }

    public static PlayerData getPlayerDataFromName(String name) {
        for (PlayerData pd : dao.getAllPlayers()) {
            if (pd.getPlayerName().equalsIgnoreCase(name)) {
                return pd;
            }
        }
        return null;
    }
}
