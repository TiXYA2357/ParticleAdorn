package TiXYA2357.ParticleAdorn;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import lombok.*;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static TiXYA2357.ParticleAdorn.Main.*;

public final class Configs {
    private Configs() {}

    /**
     * 消息前缀
     */
    public static String PT;
    /**
     * 提示前缀
     */
    public static String PA;
    /**
     * 自定义指令
     */
    @Getter
    private static String cmd;

    public static boolean initConfig(){
        inst = EconomyAPI.getInstance();
        cacheConfigs.clear();
        PT = getConfig("服务器名称", "§r§b粒子装扮 §a>>> §7");
        PA = getConfig("服务器提示", "§r§b粒子装扮 §a丨 §7");
        cmd = getConfig("指令", "plradn");

        AdornParticle_TipVal = getConfig("提示变量", AdornParticle_TipVal);
        def_use = getConfig("默认使用", def_use);
        invts = getConfig("库存", invts);
//        coinName = getConfig("货币名称", coinName);
        if (invts.isEmpty()) {
            invts.put("demo", def_invt);
            setConfig("库存", invts);
        }
        ParticleI.init();

        return true;
    }

    private static final Map<String, Config> cacheConfigs = new ConcurrentHashMap<>();
    public static <T> T getConfig(String Name, T Default){
        return getConfig("Config",Name, Default);}
    public static <T> T getConfig(String Path, String Name, T Default){
        var path = getConfigPath()+"/"+Path+".yml";
        var config = cacheConfigs.getOrDefault(path, new Config(path, Config.YAML));
        if (!cacheConfigs.containsKey(path)) cacheConfigs.put(path, config);
        if (!config.exists(Name)) config.set(Name, Default);
        config.save(); config.reload();return config.get(Name, Default);}
    public static void setConfig(String Name,Object Value){
        setConfig("Config",Name ,Value);}
    public static void setConfig(String Path,String Name,Object Value){
        var path = getConfigPath()+"/"+Path+".yml";
        var config = cacheConfigs.getOrDefault(path, new Config(path, Config.YAML));
        if (!cacheConfigs.containsKey(path)) cacheConfigs.put(path, config);
        config.set(Name, Value); config.save(); config.reload();}

    public static Config getConfigByCache(String path, String Name) {
        var newPath = getConfigPath() + "/" + (path.isEmpty() ? "" : path + "/") + Name;
        var config = cacheConfigs.getOrDefault(newPath, new Config(newPath, Config.YAML));
        if (!cacheConfigs.containsKey(newPath)) cacheConfigs.put(newPath, config);
        return config;
    }
}
