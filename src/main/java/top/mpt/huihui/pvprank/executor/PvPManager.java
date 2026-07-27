package top.mpt.huihui.pvprank.executor;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import top.mpt.huihui.pvprank.manager.PlayerData;
import top.mpt.huihui.pvprank.manager.Team;
import top.mpt.huihui.pvprank.utils.ChatUtils;
import top.mpt.huihui.pvprank.utils.ConfigUtils;
import top.mpt.huihui.pvprank.utils.LogUtils;
import top.mpt.huihui.pvprank.utils.PlayerUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static top.mpt.huihui.pvprank.PVPRank.instance;
import static top.mpt.huihui.pvprank.PVPRank.normal;

/**
 * PvP管理器：处理世界选择、Kit发放、buff、PVP开始/结束逻辑
 */
public class PvPManager {

    public static final Set<UUID> eliminatedPlayers = ConcurrentHashMap.newKeySet();
    public static final Map<Integer, Integer> teamBattleMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static String[] selectRandomWorld(boolean isSolo) {
        String configKey = isSolo ? "worlds_for_solo" : "worlds_for_team";
        List<Map<String, Object>> worldList = (List<Map<String, Object>>) 
                ConfigUtils.getDefaultConfig(configKey);
        if (worldList == null || worldList.isEmpty()) {
            LogUtils.warning("config.yml 中没有配置" + configKey);
            return null;
        }
        List<Map<String, Object>> shuffled = new ArrayList<>(worldList);
        Collections.shuffle(shuffled);
        Map<String, Object> selected = shuffled.get(0);
        for (Map.Entry<String, Object> entry : selected.entrySet()) {
            List<String> coords = (List<String>) entry.getValue();
            if (coords != null && coords.size() >= 2) {
                return new String[]{entry.getKey(), coords.get(0), coords.get(1)};
            }
        }
        return null;
    }

    public static List<String> getKitItems() {
        List<String> items = new ArrayList<>();
        try {
            File kitFile = new File(instance.getDataFolder(), "kits/kits.yml");
            if (!kitFile.exists()) instance.saveResource("kits/kits.yml", false);
            FileConfiguration kitConfig = YamlConfiguration.loadConfiguration(kitFile);
            List<String> things = kitConfig.getStringList("things");
            if (things != null) items.addAll(things);
        } catch (Exception e) {
            LogUtils.warning("读取kits.yml失败: " + e.getMessage());
        }
        return items;
    }

    public static void giveKit(Player player) {
        for (String itemId : getKitItems()) {
            try {
                World world = player.getWorld();
                // 保存当前游戏规则
                boolean oldFeedback = world.getGameRuleValue(GameRule.SEND_COMMAND_FEEDBACK);
                boolean oldCommandOutput = world.getGameRuleValue(GameRule.COMMAND_BLOCK_OUTPUT);
                // 关闭反馈（防止玩家看到命令执行结果）
                world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
                world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "give " + player.getName() + " " + itemId);
                world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, oldFeedback);
                world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, oldCommandOutput);
            } catch (Exception e) {
                LogUtils.warning("发放物品失败: " + itemId);
            }
        }
        player.updateInventory();
    }

    public static void clearAndGiveKit(Player player) {
        player.getInventory().clear();
        giveKit(player);
    }

    // ========== buff & 传送 ==========

    public static void applyBuffMinPlayers(int teamAId, int teamBId) {
        int buffMinPlayers = (int) ConfigUtils.getDefaultConfig("buff_min_players", 1);
        List<Player> teamA = TeamExecutor.getOnlineTeamMembers(teamAId);
        List<Player> teamB = TeamExecutor.getOnlineTeamMembers(teamBId);
        int diff = Math.abs(teamA.size() - teamB.size());
        if (diff < buffMinPlayers) return;
        List<Player> smallerTeam = teamA.size() < teamB.size() ? teamA : teamB;
        double extraHealth = diff * 2.0;
        for (Player p : smallerTeam) {
            double base = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(base + extraHealth);
            p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            PlayerUtils.send(p, normal + "#GREEN#由于人数劣势，获得 %d 颗心加成！", diff);
        }
    }

    public static void resetMaxHealth(Player player) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
    }

    public static void teleportPlayer(Player player, String worldName, String coords) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "mv tp " + player.getName() + " e:" + worldName + ":" + coords);
    }

    public static void sendPlayerHome(Player player) {
        Bukkit.dispatchCommand(player, "home");
    }


    // ========== 单人PVP ==========

    public static void startSoloPvP(Player playerA, Player playerB) {
        boolean start = (boolean) ConfigUtils.getDefaultConfig("start", false);
        if (!start) {
            PlayerUtils.send(playerA, normal + "#RED#PVP模式尚未开启！");
            return;
        }
        if (TeamExecutor.isPlayerInSoloPvP(playerA) || TeamExecutor.isPlayerInSoloPvP(playerB)
                || TeamExecutor.isPlayerInTeamPvP(playerA) || TeamExecutor.isPlayerInTeamPvP(playerB)) {
            PlayerUtils.send(playerA, normal + "#RED#有一方正在战斗中！");
            return;
        }
        String[] worldInfo = selectRandomWorld(true);
        if (worldInfo == null) {
            PlayerUtils.send(playerA, normal + "#RED#没有可用的PVP场地！");
            return;
        }
        TeamExecutor.setPlayerInBattle(playerA.getUniqueId().toString(), true);
        TeamExecutor.setPlayerInBattle(playerB.getUniqueId().toString(), true);
        TeamExecutor.setPlayerOpponent(playerA.getUniqueId().toString(), playerB.getUniqueId().toString());
        TeamExecutor.setPlayerOpponent(playerB.getUniqueId().toString(), playerA.getUniqueId().toString());
        clearAndGiveKit(playerA);
        clearAndGiveKit(playerB);
        teleportPlayer(playerA, worldInfo[0], worldInfo[1]);
        teleportPlayer(playerB, worldInfo[0], worldInfo[2]);
        ChatUtils.broadcast("#GREEN#%s #AQUA#与 #GREEN#%s #AQUA#的单挑开始了！",
                playerA.getName(), playerB.getName());
    }

    public static void endSoloPvP(Player winner, Player loser) {
        int soloScore = (int) ConfigUtils.getDefaultConfig("scores_solo", 20);
        TeamExecutor.deltaPlayerScore(winner, soloScore);
        TeamExecutor.deltaPlayerScore(loser, -soloScore);
        TeamExecutor.setPlayerInBattle(winner.getUniqueId().toString(), false);
        TeamExecutor.setPlayerInBattle(loser.getUniqueId().toString(), false);
        TeamExecutor.setPlayerOpponent(winner.getUniqueId().toString(), null);
        TeamExecutor.setPlayerOpponent(loser.getUniqueId().toString(), null);
        winner.getInventory().clear();
        loser.getInventory().clear();
        resetMaxHealth(winner);
        // resetMaxHealth(loser);
        sendPlayerHome(winner);
        sendPlayerHome(loser);
        ChatUtils.broadcast("#GREEN#单挑结束！#AQUA#%s #GREEN#击败了 #AQUA#%s#GREEN#！",
                winner.getName(), loser.getName());
        PlayerUtils.send(winner, normal + "#GREEN#你赢了！+%d 分！", soloScore);
        PlayerUtils.send(loser, normal + "#RED#你输了！-%d 分。", soloScore);
    }


    // ========== 团队PVP ==========

    public static void startTeamPvP(Player sender, int teamAId, int teamBId) {
        boolean start = (boolean) ConfigUtils.getDefaultConfig("start", false);
        if (!start) {
            PlayerUtils.send(sender, normal + "#RED#PVP模式尚未开启！");
            return;
        }
        Team teamA = TeamExecutor.getTeamData(teamAId);
        Team teamB = TeamExecutor.getTeamData(teamBId);
        if (teamA == null || teamB == null) {
            PlayerUtils.send(sender, normal + "#RED#指定的队伍不存在！"); return;
        }
        if (teamA.isInBattle() || teamB.isInBattle()) {
            PlayerUtils.send(sender, normal + "#RED#有一方队伍已在战斗中！"); return;
        }
        List<Player> membersA = TeamExecutor.getOnlineTeamMembers(teamAId);
        List<Player> membersB = TeamExecutor.getOnlineTeamMembers(teamBId);
        if (membersA.isEmpty() || membersB.isEmpty()) {
            PlayerUtils.send(sender, normal + "#RED#有一方队伍没有在线成员！"); return;
        }
        String[] worldInfo = selectRandomWorld(false);
        if (worldInfo == null) {
            PlayerUtils.send(sender, normal + "#RED#没有可用的团队PVP场地！"); return;
        }
        TeamExecutor.setTeamInBattle(teamAId, true);
        TeamExecutor.setTeamInBattle(teamBId, true);
        TeamExecutor.setTeamOpponent(teamAId, teamBId);
        TeamExecutor.setTeamOpponent(teamBId, teamAId);
        teamBattleMap.put(teamAId, teamBId);
        teamBattleMap.put(teamBId, teamAId);
        eliminatedPlayers.clear();
        for (Player p : membersA) {
            clearAndGiveKit(p);
            p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            teleportPlayer(p, worldInfo[0], worldInfo[1]);
        }
        for (Player p : membersB) {
            clearAndGiveKit(p);
            p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
            teleportPlayer(p, worldInfo[0], worldInfo[2]);
        }
        applyBuffMinPlayers(teamAId, teamBId);
        ChatUtils.broadcast("#GREEN#团队竞技！#AQUA#%s(%d人) #GREEN#VS #AQUA#%s(%d人)#GREEN#！",
                teamA.getName(), membersA.size(), teamB.getName(), membersB.size());
    }

    public static void handleTeamPvPDeath(Player dead) {
        Integer teamId = TeamExecutor.getPlayerNormalTeamId(dead);
        if (teamId == null) return;
        Integer opponentTeamId = teamBattleMap.get(teamId);
        if (opponentTeamId == null) return;
        eliminatedPlayers.add(dead.getUniqueId());
        dead.getInventory().clear();
//        resetMaxHealth(dead);
//        sendPlayerHome(dead);
        dead.setGameMode(GameMode.SPECTATOR);
        List<Player> teamMembers = TeamExecutor.getOnlineTeamMembers(teamId);
        boolean allEliminated = true;
        for (Player m : teamMembers) {
            if (!eliminatedPlayers.contains(m.getUniqueId())) {
                allEliminated = false; break;
            }
        }
        if (allEliminated) {
            endTeamPvP(opponentTeamId, teamId);
        } else {
            long alive = teamMembers.stream().filter(m -> !eliminatedPlayers.contains(m.getUniqueId())).count();
            ChatUtils.broadcast("#AQUA#%s #RED#被淘汰！剩余 %d 人。", dead.getName(), alive);
        }
    }

    public static void endTeamPvP(int winnerTeamId, int loserTeamId) {
        int teamScore = (int) ConfigUtils.getDefaultConfig("scores_team", 100);
        TeamExecutor.deltaTeamScore(winnerTeamId, teamScore);
        TeamExecutor.deltaTeamScore(loserTeamId, -teamScore);
        List<PlayerData> wMembers = TeamExecutor.getTeamMembers(winnerTeamId);
        List<PlayerData> lMembers = TeamExecutor.getTeamMembers(loserTeamId);
        int total = (wMembers != null ? wMembers.size() : 0) + (lMembers != null ? lMembers.size() : 0);
        int personalScore = teamScore / Math.max(1, total);
        TeamExecutor.setTeamInBattle(winnerTeamId, false);
        TeamExecutor.setTeamInBattle(loserTeamId, false);
        TeamExecutor.setTeamOpponent(winnerTeamId, null);
        TeamExecutor.setTeamOpponent(loserTeamId, null);
        teamBattleMap.remove(winnerTeamId);
        teamBattleMap.remove(loserTeamId);
        eliminatedPlayers.clear();
        for (PlayerData m : wMembers) {
            Player p = Bukkit.getPlayer(UUID.fromString(m.getUuid()));
            if (p != null && p.isOnline()) {
                p.getInventory().clear(); resetMaxHealth(p); sendPlayerHome(p);
                TeamExecutor.deltaPlayerScore(p, personalScore);
                PlayerUtils.send(p, normal + "#GREEN#团队赢了！+%d 分！", personalScore);
            }
        }
        for (PlayerData m : lMembers) {
            Player p = Bukkit.getPlayer(UUID.fromString(m.getUuid()));
            if (p != null && p.isOnline()) {
                p.setGameMode(GameMode.SURVIVAL);
                p.getInventory().clear(); resetMaxHealth(p); sendPlayerHome(p);
                TeamExecutor.deltaPlayerScore(p, -personalScore);
                PlayerUtils.send(p, normal + "#RED#团队输了！-%d 分。", personalScore);
            }
        }
        Team wt = TeamExecutor.getTeamData(winnerTeamId);
        Team lt = TeamExecutor.getTeamData(loserTeamId);
        ChatUtils.broadcast("#GREEN#团队竞技结束！#AQUA#%s #GREEN#击败 #AQUA#%s#GREEN#！",
                wt != null ? wt.getName() : "Team-" + winnerTeamId,
                lt != null ? lt.getName() : "Team-" + loserTeamId);
    }

    public static boolean isEliminated(Player player) {
        return eliminatedPlayers.contains(player.getUniqueId());
    }
}

