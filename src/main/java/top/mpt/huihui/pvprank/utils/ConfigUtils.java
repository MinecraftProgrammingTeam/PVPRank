package top.mpt.huihui.pvprank.utils;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

import static top.mpt.huihui.pvprank.PVPRank.instance;

/**
 * Config工具类
 * @author WindLeaf_qwq & X_huihui
 */
public class ConfigUtils {
    /**
     * 获取Config
     * @param config FileConfiguration
     * @param path 名称
     * @return ConfigValue
     */
    public static Object getConfig(FileConfiguration config, String path) {
        return config.get(path);
    }

    /**
     * 获取Config
     * @param config FileConfiguration
     * @param path 名称
     * @param defaultValue 默认值
     * @return ConfigValue
     */
    public static Object getConfig(FileConfiguration config, String path, Object defaultValue) {
        Object result = getConfig(config, path);
        return result == null ? defaultValue : result;
    }

    /**
     * 加载服务器默认的config.yml
     * @param path 名称
     * @return ConfigValue
     */
    public static Object getDefaultConfig(String path) {
        return getConfig(instance.getConfig(), path);
    }

    /**
     * 加载服务器默认的config.yml
     * @param path 名称
     * @param defaultValue 默认值
     * @return ConfigValue
     */
    public static Object getDefaultConfig(String path, Object defaultValue) {
        Object result = getConfig(instance.getConfig(), path);
        return result == null ? defaultValue : result;
    }

    public static List<?> getListConfig(FileConfiguration config, String path){
        return config.getList(path);
    }

}
