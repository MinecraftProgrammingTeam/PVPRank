package top.mpt.huihui.pvprank.manager;

public class Team {
    private final int id;
    private final String name;
    private final long score;
    private final boolean inBattle;
    private final Integer opponentTeamId;   // 新增

    public Team(int id, String name, long score, boolean inBattle, Integer opponentTeamId) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.inBattle = inBattle;
        this.opponentTeamId = opponentTeamId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public long getScore() { return score; }
    public boolean isInBattle() { return inBattle; }
    public Integer getOpponentTeamId() { return opponentTeamId; }   // 新增 getter
}