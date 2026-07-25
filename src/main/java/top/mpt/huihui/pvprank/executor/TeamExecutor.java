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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static top.mpt.huihui.pvprank.PVPRank.*;

/**
 * 处理玩家队伍
 * @author X_huihui、Y0uM
 */
public class TeamExecutor {
    private static final DAO dao = teamPlayerDAO;
    private static final JavaPlugin plugin = instance;
    // 单人PVP队伍编号范围
    public static final int SOLO_MIN = 100000;
    public static final int SOLO_MAX = 999999;

    /**
     * 用来添加队伍。
     * @param teamID 团队ID，纯数字编号。
     * @param teamName 团队名称。
     */
    public static void addTeam(int teamID, String teamName, CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String finalName = teamName;
            if (Objects.equals(teamName, "")) {
                finalName = "Team-" + teamID;
            }
            // 防止int_max和0和负数
            if (teamID < 0 || teamID == Integer.MAX_VALUE) {
                PlayerUtils.send(sender, normal + "#RED#您输入的数字不合规，请输入区间(0,2147483647)内的数字");
            } else if (dao.getTeam(teamID) != null) {
                PlayerUtils.send(sender, normal + "#RED#该队伍ID已经存在！");
            } else {
                dao.saveTeam(teamID, finalName, 0, false, null);
                ChatUtils.broadcast("#GREEN#PVP团队: #AQUA#%s #GREEN#已被 #AQUA#%s #GREEN#创建。团队编号为：%d", teamName, sender.getName(), teamID);
                LogUtils.info("创建队伍成功，ID: " + teamID + ", 名称: " + finalName);
                if (sender instanceof Player) {
                    addPlayer((Player) sender, teamID);
                }
            }
        });
    }

    /**
     * 单人pvp，队伍编号会在[100000,999999]范围内，其余的编号都留给团队竞技。
     * [100000,999999]内的编号会自动顺延，例如：如果100000存在，会创建100001号团队。
     * @param player 进行pvp的选手
     */
    public static void addSingleTeam(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int finalId = SOLO_MIN;
            // 若ID在单人PVP范围内，自动顺延至可用ID
            while (dao.getTeam(finalId) != null) {
                finalId++;
                if (finalId > SOLO_MAX) {
                    LogUtils.warning("单人PVP队伍编号已满，无法创建新队伍");
                    return;
                }
            }
            // 创建团队，名字默认 "Solo-玩家ID"，积分0，不在战斗中
            String finalName = "Solo-" + player.getName();
            dao.saveTeam(finalId, finalName, 0, false, null);
            LogUtils.info("创建队伍成功，ID: " + finalId + ", 名称: " + finalName);

            // 将玩家拽入该Team
            addPlayer(player, finalId);
        });
    }

    /**
     * 用来在数据库中登记玩家
     * 只会在玩家没有注册的时候登记，非常安全，一般不会造成数据覆写
     * @param player 玩家
     */
    public static void registerPlayer(Player player) {
        dao.savePlayer(
                String.valueOf(player.getUniqueId()),
                player.getName(),
                0,
                0,
                false,
                Instant.now().toEpochMilli(),
                "member",
                null);
    }

    /**
     * 用来将玩家入队。
     * 如果玩家已经在一个队伍中，并且要进行单人pvp，那么玩家队伍信息保持不变。
     * 如果玩家没有在队伍中，并且要进行单人PVP，那么将玩家移入创建的编号为[100000,999999]内的队伍
     * 单人PVP结束之后会将玩家自动移出该临时队伍，该临时队伍也随即解散。
     * @param player 玩家
     * @param teamID 团队ID
     */
    public static void addPlayer(Player player, int teamID) {
        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean isSolo = (teamID >= SOLO_MIN && teamID <= SOLO_MAX);

            // 如果已在单人PVP
            if (isSolo && isPlayerInSoloPvP(player)) {
                return;
            }

            // 获取玩家当前队伍信息
            PlayerData playerData = dao.getPlayer(uuid.toString());
            Integer currentTeamId = (playerData != null) ? playerData.getTeamId() : null;

            if (isSolo) {
                // ---------- 单人PVP ----------
                if (currentTeamId != null) {
                    // 玩家已有队伍 → 不修改数据库，只标记状态
                    dao.setPlayerInBattle(String.valueOf(uuid),true);
                    LogUtils.info(player.getName() + " 进入单人PVP模式（已有队伍）");
                } else {
                    // 将玩家加入该临时队伍
                    dao.setPlayerTeam(uuid.toString(), teamID);
                    dao.setPlayerInBattle(String.valueOf(uuid),true);
                    LogUtils.info(player.getName() + " 加入单人PVP临时队伍 " + teamID);
                }
            } else {
                if (currentTeamId != null) {
                    PlayerUtils.send(player, normal + "#RED#检测到您仍然在队伍：%d中，请退出后再加入新的队伍。", currentTeamId);
                } else {
                    // ---------- 普通团队PVP ----------
                    // 直接设置玩家队伍（覆盖原有关系）
                    dao.setPlayerTeam(uuid.toString(), teamID);
                    // 清除可能的单人PVP状态
                    dao.setPlayerInBattle(String.valueOf(uuid),false);
                    LogUtils.info(player.getName() + " 加入团队 " + teamID);
                }
            }
        });
    }

    /**
     * 解散队伍
     * @param teamID 队伍ID
     */
    public static void removeTeam(int teamID) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // 如果是单人临时队伍，先清除所有成员的solo状态
            if (teamID >= SOLO_MIN && teamID <= SOLO_MAX) {
                List<PlayerData> members = dao.getPlayersByTeam(teamID);
                for (PlayerData member : members) {
                    UUID uuid = UUID.fromString(member.getUuid());
                    dao.setPlayerInBattle(String.valueOf(uuid),false);
                }
            }
            // 删除团队（外键 ON DELETE SET NULL 自动将玩家 team_id 置空）
            dao.deleteTeam(teamID);
            LogUtils.info("队伍 " + teamID + " 已解散");
        });
    }

    /**
     * 将玩家移出队伍
     * @param player 玩家
     */
    public static void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (isPlayerInSoloPvP(player)) {
                if (isPlayerInSoloTeam(player)) {
                    PlayerData playerData = dao.getPlayer(uuid.toString());
                    int currentTeamId = playerData.getTeamId();
                    // 将玩家从临时队伍移出
                    dao.setPlayerTeam(uuid.toString(), null);
                    // 检查该临时队伍是否还有成员
                    List<PlayerData> members = dao.getPlayersByTeam(currentTeamId);
                    if (members.isEmpty()) {
                        dao.deleteTeam(currentTeamId);
                        LogUtils.info("单人PVP临时队伍：" + currentTeamId + " 已解散（空）");
                    }
                }
                // 移除单人PVP状态（无论是否在临时队伍）
                dao.setPlayerInBattle(String.valueOf(uuid),false);
                LogUtils.info(player.getName() + " 已退出单人PVP");
            } else {
                // ---------- 退出普通团队 ----------
                dao.setPlayerTeam(uuid.toString(), null);
                LogUtils.info(player.getName() + " 已移出队伍");
            }
        });
    }

    /**
     * 检查玩家是否处于单人PVP状态
     * @param player 玩家
     * @return 状态
     */
    public static boolean isPlayerInSoloPvP(Player player) {
        PlayerData playerData = dao.getPlayer(player.getUniqueId().toString());
        return playerData.isInBattle();
    }

    /**
     * 检查玩家是否处于SoloTeam
     * @param player 玩家ID
     * @return 返回值
     */
    public static boolean isPlayerInSoloTeam(Player player) {
        PlayerData playerData = dao.getPlayer(player.getUniqueId().toString());
        Integer currentTeamId = (playerData != null) ? playerData.getTeamId() : null;
        return currentTeamId != null && currentTeamId >= SOLO_MIN && currentTeamId <= SOLO_MAX;
    }


    /**
     * 检测玩家是否处于TeamPVP状态
     * @param player 玩家
     * @return 状态
     */
    public static boolean isPlayerInTeamPvP(Player player) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        Integer currentTeamId = (playerData != null) ? playerData.getTeamId() : null;
        if (currentTeamId != null && (currentTeamId < SOLO_MIN || currentTeamId > SOLO_MAX)){
            return dao.getTeam(currentTeamId).isInBattle();
        } else {
            return false;
        }
    }

    /**
     * 初始化默认队伍，编号为0，名称为"Default"
     * 如果已存在则跳过，否则创建
     */
    public static void initializeDefaultTeam() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Team team = dao.getTeam(0);
            if (team == null) {
                dao.saveTeam(0, "Default", 0, false, null);
                LogUtils.info("已创建默认队伍 Default (ID: 0)");
                LogUtils.info("请管理员利用/pvprank setPermission <管理员ID> operator来使得管理员可以管理玩家的初始Team");
            } else {
                LogUtils.info("默认队伍 Default 已存在，无需创建");
            }
        });
    }

    /**
     * 检查玩家是否处于数据库中（录入数据库来用）（多线程）
     * @param player 玩家
     * @param callback 回调
     */
    public static void checkPlayerExistsAsync(Player player, java.util.function.Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean exists = dao.existsPlayer(player.getUniqueId().toString());
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(exists));
        });
    }


    public static PlayerData getPlayerData(UUID uuid) {
        return dao.getPlayer(uuid.toString());
    }
}