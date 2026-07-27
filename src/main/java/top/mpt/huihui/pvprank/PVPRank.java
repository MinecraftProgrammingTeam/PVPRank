package top.mpt.huihui.pvprank;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import top.mpt.huihui.pvprank.executor.TeamExecutor;
import top.mpt.huihui.pvprank.commands.CommandHandler;
import top.mpt.huihui.pvprank.events.onPlayerDeath;
import top.mpt.huihui.pvprank.events.onPlayerJoinAndQuit;
import top.mpt.huihui.pvprank.gui.GUIListener;
import top.mpt.huihui.pvprank.gui.GUIManager;
import top.mpt.huihui.pvprank.manager.DAO;
import top.mpt.huihui.pvprank.manager.DatabaseManager;
import top.mpt.huihui.pvprank.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public final class PVPRank extends JavaPlugin {

    public static PVPRank instance;
    public static String normal = "#AQUA#[PVP] ";
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

        /* 数据库操作 */
        dbManager = new DatabaseManager(getDataFolder().getAbsolutePath());
        teamPlayerDAO = new DAO(dbManager);

        // 异步建表
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            teamPlayerDAO.createTables();
            LogUtils.info("#AQUA#数据库准备就绪");
        });
        // 初始化Team
//         TeamExecutor.initializeDefaultTeam();


        /* 指令，事件操作 */
        getCommand("pvprank").setExecutor(new CommandHandler());

        /* 注册事件监听器 */
        Bukkit.getPluginManager().registerEvents(new onPlayerDeath(), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new onPlayerJoinAndQuit(), this);

        /* /pvp 打开GUI */
        getCommand("pvp").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player) {
                GUIManager.openMainMenu((Player) sender);
            }
            return true;
        });

        /* 检查是否装有MV插件 */
        // 注意：插件名称是 "Multiverse-Core"，大小写敏感
        Plugin mvPlugin = Bukkit.getPluginManager().getPlugin("Multiverse-Core");

        if (mvPlugin != null && mvPlugin.isEnabled()) {
            // Multiverse-Core 插件存在且已启用
            LogUtils.info("Multiverse-Core 已找到并启用！");
        } else {
            // 插件不存在或未启用
            LogUtils.warning("未检测到 Multiverse-Core，本插件将不可工作。");
        }


    }

    @Override
    public void onDisable() {
        // 务必关闭连接池，释放数据库文件句柄
        if (dbManager != null) {
            dbManager.close();
            LogUtils.info("数据库连接已释放");
        }
    }
}
