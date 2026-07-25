package top.mpt.huihui.pvprank.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理两张表：
 * 1. teams（团队）: id(INT主键), name(TEXT), score(LONG), in_battle(BOOL),
 * opponent(INT)(ID)
 * 2. players（玩家）: uuid(TEXT主键), player_name(TEXT), team_id(INT),
 * personal_score(LONG), in_battle(BOOL), join_time(TIMESTAMP),
 * permission(owner, op, mem), opponent(TEXT)(uuid)
 * 外键：players.team_id 关联 teams.id，删除团队时玩家 team_id 置为 NULL
 */
public class DAO {

    private final DatabaseManager dbManager;

    public DAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // ========== 建表 ==========
    public void createTables() {
        // 开启外键约束（SQLite默认关闭）
        String pragma = "PRAGMA foreign_keys = ON;";

        String createTeams = "CREATE TABLE IF NOT EXISTS teams (" +
                "id INTEGER PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "score INTEGER DEFAULT 0, " +
                "in_battle INTEGER DEFAULT 0, " +
                "opponent_team_id INTEGER DEFAULT NULL" +   // 新增：对手队伍ID
                ")";

        String createPlayers = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid TEXT PRIMARY KEY, " +
                "player_name TEXT NOT NULL, " +
                "team_id INTEGER, " +
                "personal_score INTEGER DEFAULT 0, " +
                "in_battle INTEGER DEFAULT 0, " +
                "join_time TIMESTAMP DEFAULT NULL, " +
                "permission VARCHAR(20) DEFAULT 'member', " +
                "opponent_uuid VARCHAR(36) DEFAULT NULL, " +   // 新增：对手玩家UUID
                "FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL" +
                ")";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pragmaStmt = conn.prepareStatement(pragma);
             PreparedStatement teamStmt = conn.prepareStatement(createTeams);
             PreparedStatement playerStmt = conn.prepareStatement(createPlayers)) {

            pragmaStmt.executeUpdate();
            teamStmt.executeUpdate();
            playerStmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== 团队操作 ==========

    // 插入或更新团队（存在则更新，不存在则插入）
    public void saveTeam(int id, String name, long score, boolean inBattle, Integer opponentTeamId) {
        String sql = "INSERT INTO teams (id, name, score, in_battle, opponent_team_id) VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET " +
                "name = excluded.name, " +
                "score = excluded.score, " +
                "in_battle = excluded.in_battle, " +
                "opponent_team_id = excluded.opponent_team_id";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setLong(3, score);
            stmt.setInt(4, inBattle ? 1 : 0);
            if (opponentTeamId == null) {
                stmt.setNull(5, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(5, opponentTeamId);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 查询团队（返回 Team 对象，需要自己定义简单 POJO，或直接用数组）
    public Team getTeam(int id) {
        String sql = "SELECT id, name, score, in_battle, opponent_team_id FROM teams WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Integer opp = rs.getInt("opponent_team_id");
                    if (rs.wasNull()) opp = null;
                    return new Team(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getLong("score"),
                            rs.getInt("in_battle") == 1,
                            opp
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // 获取所有团队
    public List<Team> getAllTeams() {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT id, name, score, in_battle, opponent_team_id FROM teams ORDER BY id";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Integer opp = rs.getInt("opponent_team_id");
                if (rs.wasNull()) opp = null;
                list.add(new Team(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getLong("score"),
                        rs.getInt("in_battle") == 1,
                        opp
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 删除团队（由于外键 ON DELETE SET NULL，玩家表中的 team_id 会自动置为 NULL）
    public void deleteTeam(int id) {
        String sql = "DELETE FROM teams WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 增加团队积分（增量更新）
    public void addTeamScore(int id, long delta) {
        String sql = "UPDATE teams SET score = score + ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, delta);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 设置团队是否在蘸豆
    public void setTeamInBattle(int id, boolean inBattle) {
        String sql = "UPDATE teams SET in_battle = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, inBattle ? 1 : 0);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置团队对手
     * @param teamId 团队ID
     * @param opponentTeamId 对手ID
     */
    public void setTeamOpponent(int teamId, Integer opponentTeamId) {
        String sql = "UPDATE teams SET opponent_team_id = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (opponentTeamId == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, opponentTeamId);
            }
            stmt.setInt(2, teamId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== 玩家操作 ==========

    /**
     * 插入或更新玩家数据库信息
     * @param uuid UUID
     * @param playerName 玩家ID
     * @param teamId 玩家所属团队ID
     * @param personalScore 玩家个人积分
     * @param inBattle 是否在solo中
     * @param joinTime 加入时间
     * @param permission 权限(owner/operator/member)
     * @param opponentUuid 对手UUID
     */
    public void savePlayer(String uuid, String playerName, Integer teamId, long personalScore,
                           boolean inBattle, Long joinTime, String permission, String opponentUuid) {
        String sql = "INSERT INTO players (uuid, player_name, team_id, personal_score, in_battle, join_time, permission, opponent_uuid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET " +
                "player_name = excluded.player_name, " +
                "team_id = excluded.team_id, " +
                "personal_score = excluded.personal_score, " +
                "in_battle = excluded.in_battle, " +
                "join_time = excluded.join_time, " +
                "permission = excluded.permission, " +
                "opponent_uuid = excluded.opponent_uuid";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, playerName);
            if (teamId == null) {
                stmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(3, teamId);
            }
            stmt.setLong(4, personalScore);
            stmt.setInt(5, inBattle ? 1 : 0);
            if (joinTime == null) {
                stmt.setNull(6, java.sql.Types.TIMESTAMP);
            } else {
                stmt.setTimestamp(6, new java.sql.Timestamp(joinTime));
            }
            stmt.setString(7, permission);
            if (opponentUuid == null) {
                stmt.setNull(8, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(8, opponentUuid);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 查询玩家
     * @param uuid UUID
     * @return PlayerData
     */
    public PlayerData getPlayer(String uuid) {
        String sql = "SELECT uuid, player_name, team_id, personal_score, in_battle, join_time, permission, opponent_uuid FROM players WHERE uuid = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long joinTime = rs.getTimestamp("join_time") == null ? null : rs.getTimestamp("join_time").getTime();
                    int rawTeamId = rs.getInt("team_id");
                    Integer teamId = rs.wasNull() ? null : rawTeamId;
                    String opp = rs.getString("opponent_uuid");
                    if (rs.wasNull()) opp = null;
                    return new PlayerData(
                            rs.getString("uuid"),
                            rs.getString("player_name"),
                            teamId,
                            rs.getLong("personal_score"),
                            rs.getInt("in_battle") == 1,
                            joinTime,
                            rs.getString("permission"),
                            opp
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据团队 ID 获取该团队所有玩家
     * @param teamId 团队ID
     * @return 玩家列表
     */
    public List<PlayerData> getPlayersByTeam(int teamId) {
        List<PlayerData> list = new ArrayList<>();
        String sql = "SELECT uuid, player_name, team_id, personal_score, in_battle, join_time, permission, opponent_uuid FROM players WHERE team_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long joinTime = rs.getTimestamp("join_time") == null ? null : rs.getTimestamp("join_time").getTime();
                    int rawTeamId = rs.getInt("team_id");
                    Integer teamIdResult = rs.wasNull() ? null : rawTeamId;
                    String opp = rs.getString("opponent_uuid");
                    if (rs.wasNull()) opp = null;
                    list.add(new PlayerData(
                            rs.getString("uuid"),
                            rs.getString("player_name"),
                            teamIdResult,
                            rs.getLong("personal_score"),
                            rs.getInt("in_battle") == 1,
                            joinTime,
                            rs.getString("permission"),
                            opp
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 删除玩家
    public void deletePlayer(String uuid) {
        String sql = "DELETE FROM players WHERE uuid = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新玩家个人积分（增量）
    public void addPlayerScore(String uuid, long delta) {
        String sql = "UPDATE players SET personal_score = personal_score + ? WHERE uuid = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, delta);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更改玩家所属团队（将 team_id 设为某个团队 ID，或置为 NULL）
    public void setPlayerTeam(String uuid, Integer teamId) {
        String sql;
        if (teamId == null) {
            // 如果退出团队，将 team_id 和 join_time 都置为 NULL
            sql = "UPDATE players SET team_id = NULL, join_time = NULL WHERE uuid = ?";
        } else {
            // 如果加入团队，将 join_time 设为当前时间
            sql = "UPDATE players SET team_id = ?, join_time = CURRENT_TIMESTAMP WHERE uuid = ?";
        }
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (teamId == null) {
                stmt.setString(1, uuid);
            } else {
                stmt.setInt(1, teamId);
                stmt.setString(2, uuid);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更改玩家的蘸豆状态
    public void setPlayerInBattle(String uuid, boolean inBattle) {
        String sql = "UPDATE players SET in_battle = ? WHERE uuid = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, inBattle ? 1 : 0);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 设置玩家权限
    public void setPlayerPermission(String uuid, String permission) {
        String sql = "UPDATE players SET permission = ? WHERE uuid = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, permission);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断玩家是否在数据库中存在
     * @param uuid 玩家UUID
     * @return true=存在，false=不存在
     */
    public boolean existsPlayer(String uuid) {
        String sql = "SELECT 1 FROM players WHERE uuid = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();  // 有结果即存在
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 设置玩家的对抗状态
     * @param uuid 玩家UUID
     * @param opponentUuid 对手UUID
     */
    public void setPlayerOpponent(String uuid, String opponentUuid) {
        String sql = "UPDATE players SET opponent_uuid = ? WHERE uuid = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (opponentUuid == null) {
                stmt.setNull(1, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(1, opponentUuid);
            }
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}