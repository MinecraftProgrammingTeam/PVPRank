package top.mpt.huihui.pvprank.manager;

public class Team {
    private final int id;
    private final String name;
    private final long score;
    private final boolean inBattle;   // 新增

    public Team(int id, String name, long score, boolean inBattle) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.inBattle = inBattle;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public long getScore() { return score; }
    public boolean isInBattle() { return inBattle; }   // 新增 getter
}