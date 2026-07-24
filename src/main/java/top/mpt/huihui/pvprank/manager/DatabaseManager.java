package top.mpt.huihui.pvprank.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private final HikariDataSource dataSource;

    public DatabaseManager(String dataFolderPath) {
        HikariConfig config = new HikariConfig();
        // 数据库文件存放在插件文件夹下
        String jdbcUrl = "jdbc:sqlite:" + dataFolderPath + "/player_stats.db" +
                "?journal_mode=WAL" +          // 启用WAL模式，提高并发
                "&synchronous=NORMAL" +        // 平衡性能与安全
                "&cache_size=5000";            // 缓存大小
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(10);         // 小型服务器10个连接足够
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);    // 30秒超时

        this.dataSource = new HikariDataSource(config);
    }

    // 获取连接（供DAO层调用）
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // 插件卸载时关闭连接池，释放资源
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}