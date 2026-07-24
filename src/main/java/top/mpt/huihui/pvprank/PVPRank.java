package top.mpt.huihui.pvprank;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.commands.CommandHandler;
import top.mpt.huihui.pvprank.manager.DAO;
import top.mpt.huihui.pvprank.manager.DatabaseManager;
import top.mpt.huihui.pvprank.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public final class PVPRank extends JavaPlugin {

    public static PVPRank instance;
    public static String normal = "[PVP] ";
    public static TeamExecutor teamExecutor;
    public static boolean modeStarted = false;
    public static List<String> Online_Players = new ArrayList<>();

    // 数据库
    private DatabaseManager dbManager;
    public static DAO teamPlayerDAO;

    @Override
    public void onEnable() {
        instance = this;
        // config
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Bukkit.getOnlinePlayers().forEach(it -> Online_Players.add(it.getName()) );

        /* 数据库操作 */
        dbManager = new DatabaseManager(getDataFolder().getAbsolutePath());
        teamPlayerDAO = new DAO(dbManager);

        // 异步建表
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            teamPlayerDAO.createTables();
            LogUtils.info(normal + "#AQUA#数据库准备就绪");
        });

//        teamExecutor = new TeamExecutor(this, teamPlayerDAO);

        /* 指令，事件操作 */
        getCommand("pvprank").setExecutor(new CommandHandler());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
