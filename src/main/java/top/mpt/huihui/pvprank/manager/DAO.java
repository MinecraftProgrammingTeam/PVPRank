package top.mpt.huihui.pvprank.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理两张表：
 * 1. teams（团队）: id(INT主键), name(TEXT), score(LONG)，in_battle(BOOL)
 * 2. players（玩家）: uuid(TEXT主键), player_name(TEXT), team_id(INT), personal_score(LONG)，in_battle(BOOL)
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
                "in_battle INTEGER DEFAULT 0" +     // 0 = false, 1 = true
                ")";

        String createPlayers = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid TEXT PRIMARY KEY, " +
                "player_name TEXT NOT NULL, " +
                "team_id INTEGER, " +
                "personal_score INTEGER DEFAULT 0, " +
                "in_battle INTEGER DEFAULT 0, " +
                "join_time TIMESTAMP DEFAULT NULL, " +
                "permission VARCHAR(20) DEFAULT 'member', " +   // 新增：owner/operator/member
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
    public void saveTeam(int id, String name, long score, boolean inBattle) {
        String sql = "INSERT INTO teams (id, name, score, in_battle) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET name = excluded.name, score = excluded.score, in_battle = excluded.in_battle";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setLong(3, score);
            stmt.setInt(4, inBattle ? 1 : 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 查询团队（返回 Team 对象，需要自己定义简单 POJO，或直接用数组）
    public Team getTeam(int id) {
        String sql = "SELECT id, name, score, in_battle FROM teams WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Team(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getLong("score"),
                            rs.getInt("in_battle") == 1
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
        String sql = "SELECT id, name, score, in_battle FROM teams ORDER BY id";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Team(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getLong("score"),
                        rs.getInt("in_battle") == 1
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

    // ========== 玩家操作 ==========

    // 插入或更新玩家
    public void savePlayer(String uuid, String playerName, int teamId, long personalScore,
                           boolean inBattle, Long joinTime, String permission) {
        String sql = "INSERT INTO players (uuid, player_name, team_id, personal_score, in_battle, join_time, permission) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET " +
                "player_name = excluded.player_name, " +
                "team_id = excluded.team_id, " +
                "personal_score = excluded.personal_score, " +
                "in_battle = excluded.in_battle, " +
                "join_time = excluded.join_time, " +
                "permission = excluded.permission";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, playerName);
            stmt.setInt(3, teamId);
            stmt.setLong(4, personalScore);
            stmt.setInt(5, inBattle ? 1 : 0);
            if (joinTime == null) {
                stmt.setNull(6, java.sql.Types.TIMESTAMP);
            } else {
                stmt.setTimestamp(6, new java.sql.Timestamp(joinTime));
            }
            stmt.setString(7, permission);   // 新增
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 查询玩家（返回 PlayerData 对象）
    public PlayerData getPlayer(String uuid) {
        String sql = "SELECT uuid, player_name, team_id, personal_score, in_battle, join_time, permission FROM players WHERE uuid = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long joinTime = rs.getTimestamp("join_time") == null ? null : rs.getTimestamp("join_time").getTime();
                    return new PlayerData(
                            rs.getString("uuid"),
                            rs.getString("player_name"),
                            rs.getInt("team_id"),
                            rs.getLong("personal_score"),
                            rs.getInt("in_battle") == 1,
                            joinTime,
                            rs.getString("permission")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 根据团队 ID 获取该团队所有玩家
    public List<PlayerData> getPlayersByTeam(int teamId) {
        List<PlayerData> list = new ArrayList<>();
        String sql = "SELECT uuid, player_name, team_id, personal_score, in_battle, join_time, permission FROM players WHERE team_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long joinTime = rs.getTimestamp("join_time") == null ? null : rs.getTimestamp("join_time").getTime();
                    list.add(new PlayerData(
                            rs.getString("uuid"),
                            rs.getString("player_name"),
                            rs.getInt("team_id"),
                            rs.getLong("personal_score"),
                            rs.getInt("in_battle") == 1,
                            joinTime,
                            rs.getString("permission")
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


}