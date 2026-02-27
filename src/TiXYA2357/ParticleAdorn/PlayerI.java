package TiXYA2357.ParticleAdorn;

import cn.nukkit.utils.Config;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static TiXYA2357.ParticleAdorn.Configs.PT;
import static TiXYA2357.ParticleAdorn.Configs.getConfigByCache;
import static TiXYA2357.ParticleAdorn.Main.*;

public class PlayerI {
    public static final Map<String, PlayerI> pic = new ConcurrentHashMap<>();
    public static PlayerI getOrDefaultPlayerI(String uid) {
        var path = new File(getConfigPath() + "/Players/");
        var IFile = new File(path + "/" + uid + ".yml");
        if (!path.exists() && !path.mkdirs()) {
            nks.getLogger().error(PT + "创建文件夹 " + path.getPath() + " 失败!");
            return null;
        }
        var isFirst = !IFile.exists();
        if (isFirst) nks.getPlayer(UUID.fromString(uid)).ifPresent(p -> {
            try {
                if (!IFile.createNewFile()) p.kick("§c数据文件创建失败!",false);
            } catch (IOException e) {
                nks.getLogger().error(PT + "玩家" + p.getName() + "数据文件创建失败!");
            }
        });
        return IFile.exists() ? pic.computeIfAbsent(uid, v -> new PlayerI(uid)) : null;
    }
    public PlayerI(String uuid){
        nks.getPlayer(UUID.fromString(uuid)).ifPresent(plr -> {
            this.uuid = uuid;
            this.name = plr.getName();
            this.load();
        });
    }


    Config cfg;
    @Getter
    String uuid = "";
    @Getter
    String name = "";


    public int performanceOptimizationLevel = 5;

    public Map<String, String> AdornUse = new ConcurrentHashMap<>();
    public Map<String, HashMap<String, String>> AdornHas = new ConcurrentHashMap<>();


    public void load() {
        cfg = getConfigByCache("Players", uuid + ".yml");
        performanceOptimizationLevel = cfg.get("性能优化等级", performanceOptimizationLevel);
        AdornUse = cfg.get("装扮_使用", AdornUse);
        AdornHas = cfg.get("装扮_拥有", AdornHas);

    }

    public void save() {
        cfg = getConfigByCache("Players", uuid + ".yml");
        cfg.set("性能优化等级", performanceOptimizationLevel);
        cfg.set("装扮_使用", AdornUse);
        cfg.set("装扮_拥有", AdornHas);

        cfg.save();
    }

}
