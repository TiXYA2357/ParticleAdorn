package TiXYA2357.ParticleAdorn;

import TiXYA2357.ParticleAdorn.UI.CMListener;
import TiXYA2357.ParticleAdorn.UI.MenuBigChest;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.plugin.*;
import lombok.*;
import me.onebone.economyapi.EconomyAPI;
import tip.utils.variables.BaseVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static TiXYA2357.ParticleAdorn.Configs.*;
import static TiXYA2357.ParticleAdorn.ParticleI.executors;
import static TiXYA2357.ParticleAdorn.PlayerI.getOrDefaultPlayerI;
import static TiXYA2357.ParticleAdorn.Utils.*;
import static java.lang.Math.min;

public final class Main extends PluginBase {
    @Getter
    private static String ConfigPath;
    @Getter
    private static Plugin plugin;
    public final static Server nks = Server.getInstance();

    @Override
    public void onLoad() {
        ConfigPath = getDataFolder().getAbsolutePath();
        plugin = this;
        getLogger().info(PT + "粒子装扮系统插件已加载");
    }
    @Override
    public void onEnable() {
        initConfig();
        checkServer();
        this.getServer().getCommandMap().register(Configs.getCmd(), new MainCommand(Configs.getCmd()));
        this.getServer().getScheduler().scheduleRepeatingTask(new RepeatTask(this), 20);
        this.getServer().getPluginManager().registerEvents(new CMListener(), this);
        if (hasClazz(() -> BaseVariable.class)) TipVariable.init();
        getLogger().info(PT + "粒子装扮系统插件已开启");
    }

    @Override
    public void onDisable() {
        if (executors != null) executors.shutdown();
        nks.getOnlinePlayers().values().forEach(p -> getOrDefaultPlayerI(p.getUniqueId() + "").save());
        getLogger().info(PT + "粒子装扮系统插件已关闭");
    }











    static final String title = "粒子";

//    static String coinName = title + "币";

    static String AdornParticle_TipVal = "{xisr}";
    static String def_use = "Null";
    static Map<String, HashMap<String,Object>> invts = new HashMap<>();

    static final HashMap<String, Object> def_invt = new HashMap<>(){{
        put("价格", 1000);
        put("时效", 300);
        put("描述", "一个" + title);
        put("上架",false);
        put("菜单物品", "421:0");
        put("附魔特效",false);
        put("粒子", new ArrayList<>());
    }};


    static final String ParticleHas = title + "_已拥有";
    static final String ParticleUse = title + "_使用";


    public static HashMap<String,String> getPlayerParticles(Player p) {
        return getPlayerParticles(p.getUniqueId() + "");
    }
    // 获取玩家已拥有的(名称: yy-mm-dd时间)
    public static HashMap<String,String> getPlayerParticles(String uuid) {
        return getOrDefaultPlayerI(uuid).AdornHas.getOrDefault(ParticleHas, new HashMap<>());
    }

    public static String getPlayerUseParticle(Player p) {
        return getPlayerUseParticle(p.getUniqueId() + "");
    }
    public static String getPlayerUseParticle(String uuid) {
        return getOrDefaultPlayerI(uuid).AdornUse.getOrDefault(ParticleUse, def_use);
    }

    public static String getPlayerUseParticleIsReplaceName(Player p) {
        return getPlayerUseParticleIsReplaceName(p.getUniqueId() + "");
    }
    public static String getPlayerUseParticleIsReplaceName(String uuid) {
        return getPlayerUseParticle(uuid).replace("@p", getOrDefaultPlayerI(uuid).getName());
    }

    public static boolean setPlayerUseParticle(Player p, String val) {
        return setPlayerUseParticle(p.getUniqueId() + "", val);
    }

    public static boolean setPlayerUseParticle(String uuid, String val) {
        if ((!invts.containsKey(val) || !getPlayerParticles(uuid).containsKey(val)) && !val.equals(def_use)) return false;
        getOrDefaultPlayerI(uuid).AdornUse.put(ParticleUse, val);
        return true;
    }

    public static boolean addPlayerParticle(Player p, String val, long time) {
        return addPlayerParticle(p.getUniqueId() + "", val,  time);
    }
    public static boolean addPlayerParticle(String uuid, String val, long time) {
        if (!invts.containsKey(val)) return false;
        var ts = getPlayerParticles(uuid);
        var tt = ts.getOrDefault(val, secondsToDateTime(0));
        if (time > 0) {
            if (!tt.equals("永久")) ts.put(val,secondsToDateTime(secondsLeft(tt) + time));
            else return false;
        } else if (tt.equals("永久")) return false;
        else ts.put(val, "永久");
        getOrDefaultPlayerI(uuid).AdornHas.put(ParticleHas, ts);
        setPlayerUseParticle(uuid,val);
        return true;
    }

    public static boolean setPlayerParticle(Player p, String val, long time) {
        return setPlayerParticle(p.getUniqueId() + "", val,  time);
    }
    public static boolean setPlayerParticle(String uuid, String val, long time) {
        if (!invts.containsKey(val)) return false;
        var ts = getPlayerParticles(uuid);
        if (time < 1) ts.put(val, "永久");
        else {
            var tt= ts.getOrDefault(val, secondsToDateTime(0));
            if (tt.equals("永久")) return true;
            ts.put(val, secondsToDateTime(time));
        }
        getOrDefaultPlayerI(uuid).AdornHas.put(ParticleHas, ts);
        return true;
    }


    // 如果时间小于1则是移除
    public static boolean delPlayerParticle(Player p, String val, long time) {
        return delPlayerParticle(p.getUniqueId() + "", val,  time);
    }
    public static boolean delPlayerParticle(String uuid, String val, long time) {
        var ts = getPlayerParticles(uuid);
        if (!ts.containsKey(val)) return true;
        if (time < 1) ts.remove(val);
        else {
            var tt= ts.getOrDefault(val, secondsToDateTime(0));
            if (tt.equals("永久")) return false;
            var ss= secondsLeft(tt);
            if (ss > time) ts.put(val, secondsToDateTime(ss - time));
            else ts.remove(val);
        }
        if (getPlayerUseParticle(uuid).equals(val)) setPlayerUseParticle(uuid,def_use);
        getOrDefaultPlayerI(uuid).AdornHas.put(ParticleHas, ts);
        return true;
    }

    static void chest_myHas(Player p, int offset ,int limit) {
        limit = min(45,limit);
        var l = limit;
        Async(() -> {
            var ts = getPlayerParticles(p);
            if (ts.isEmpty()) {
                p.sendPopup(PA + "你还没有"+title+"呢");
                return;
            }
            var menu = new MenuBigChest("§b我的"+title,true,true);
            ts.forEach((key,val) -> {
                var num = 0;
                if (invts.containsKey(key)) {
                    ++ num; // 这里记录成功显示的数量
                    var isUse = getPlayerUseParticle(p).equals(key);
                    var i = invts.get(key);
                    var item = getItemFromStrId((String) i.getOrDefault("菜单物品", "421:0"));
                    if ((boolean) i.getOrDefault("附魔特效",false)) item.addEnchantment(Enchantment.get(22));
                    var showKey = key.replace("@p", getOrDefaultPlayerI(p.getUniqueId() + "").getName());
                    item.setCustomName("§r§f" + showKey);
                    var desc = i.getOrDefault("描述","").toString();
                    item.setLore("""
                    {%1}
                    §r§7到期时间: {%2}
                    
                    §r{%3}
                    """.replace("{%1}",desc.isEmpty() ? "" : "\n§r" +
                                    desc.replace("&","§") + "\n")
                            .replace("{%2}",val.equals(secondsToDateTime(0)) ? "§a永久" : "§f" + val)
                            .replace("{%3}",isUse ? "§a正在使用" : "§f点击使用")
                            .replace("\\n","\n")
                    );
                    if (num <= l) menu.add(num, item, ()-> {
                        if (isUse) {
                            p.sendMessage(PT + "成功卸下§f " + showKey);
                            setPlayerUseParticle(p, def_use);
                        } else {
                            if (setPlayerUseParticle(p, key)) p.sendMessage(PT + "成功使用§f " + showKey);
                            else p.sendMessage(PT + "装备失败");
                        }
                    });
                }
            });
            if (offset > 0) menu.add(48, ChestMenuTo(Item.get(Item.ARROW),
                    1,0,"上一页"), () -> chest_myHas(p, offset - l, l));
            if (offset + l < ts.size()) menu.add(52, ChestMenuTo(Item.get(Item.ARROW),
                    1,0,"下一页"), () -> chest_myHas(p, offset + l, l));
            menu.show(p);
        });
    }

    static void chest_shop(Player p, int offset ,int limit) {
        limit = min(45,limit);
        var l = limit;
        Async(() -> {
            if (invts.isEmpty()) {
                p.sendMessage(PT + "暂时没有"+title+"出售呢");
                return;
            }
            var menu = new MenuBigChest("§b"+title+"商店",true,true);
            var num = new AtomicInteger(1);
            invts.forEach((key, val) -> {
                if (!(boolean) val.getOrDefault("上架",false)) return;
                var price = (int) val.getOrDefault("价格",0);
                var item = getItemFromStrId((String) val.getOrDefault("菜单物品", "421:0"));
                var has = getPlayerParticles(p);
                var time = (long) (int) val.getOrDefault("时效",300);
                var hasItem = has.containsKey(key);
                var hasTime = has.getOrDefault(key,secondsToDateTime(0));
                var showKey = key.replace("@p", getOrDefaultPlayerI(p.getUniqueId() + "").getName());
                if ((boolean) val.getOrDefault("附魔特效",false)) item.addEnchantment(Enchantment.get(22));
                item.setCustomName("§r§f" + showKey);
                item.setLore("""
                    §r§7价格: {%1}
                    §r§f时效: {%2}
                    §r§7描述: {%3}
                    
                    {%4}
                    """.replace("{%1}", price > 0 ? "§f" + price : "§a免费")
                        .replace("{%2}", time < 1 ? "§a永久" : "§f" + secondsToDateTime(time))
                        .replace("{%3}", val.getOrDefault("描述","").toString())
                        .replace("{%4}", hasItem ? "§a已拥有"
                                + (hasTime.equals("永久") ? "永久" : ",再次购买") : "§6点击购买")
                );
                menu.add(num.get(), item, () -> {
                    if (price > 0 && inst.myMoney(p) < price) {
                        p.sendMessage(PT + "余额不足,无法购买");
                        return;
                    }
                    if (hasTime.equals("永久")) {
                        p.sendMessage(PT + "已拥有永久期限,无法购买");
                        return;
                    }
                    inst.reduceMoney(p, price);
                    var allTime = time < 1 ? "永久" : secondsToDateTime(secondsLeft(hasTime) + time);
                    p.sendMessage(PT + "成功花费 " + price + " 购买 " + showKey + " 至§f " + allTime);
                    addPlayerParticle(p,key,time);
                    setPlayerUseParticle(p,key);
                });
                num.incrementAndGet();
            });
            if (offset > 0) menu.add(48, ChestMenuTo(Item.get(Item.ARROW),
                    1,0,"上一页"), () -> chest_shop(p, offset - l, l));
            if (offset + l < invts.size()) menu.add(52, ChestMenuTo(Item.get(Item.ARROW),
                    1,0,"下一页"), () -> chest_shop(p, offset + l, l));
            menu.show(p);
        });
    }

    static EconomyAPI inst = null;

}
