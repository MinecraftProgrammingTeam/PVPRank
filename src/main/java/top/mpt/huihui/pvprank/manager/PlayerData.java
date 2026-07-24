package top.mpt.huihui.pvprank.manager;

public class PlayerData {
    private final String uuid;
    private final String playerName;
    private final int teamId;
    private final long personalScore;
    private final boolean inBattle;
    private final Long joinTime;
    private final String permission;   // 新增

    public PlayerData(String uuid, String playerName, int teamId, long personalScore,
                      boolean inBattle, Long joinTime, String permission) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.teamId = teamId;
        this.personalScore = personalScore;
        this.inBattle = inBattle;
        this.joinTime = joinTime;
        this.permission = permission;
    }

    public String getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public int getTeamId() { return teamId; }
    public long getPersonalScore() { return personalScore; }
    public boolean isInBattle() { return inBattle; }
    public Long getJoinTime() { return joinTime; }
    public String getPermission() { return permission; }   // 新增 getter
}