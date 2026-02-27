package TiXYA2357.ParticleAdorn;

import TiXYA2357.ParticleAdorn.UI.AbstractFakeInventory;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

import static TiXYA2357.ParticleAdorn.Main.*;

public class Utils {

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    static {DATE_TIME_FORMAT.setTimeZone(TimeZone.getDefault());}
    /**
     * 计算指定日期时间距离现在还有多少秒
     * @param dateTimeStr 日期时间字符串，格式为 "yy-MM-dd HH:mm:ss"
     * @return 距离现在还有多少秒，如果已经过期则返回负数
     */
    public static long secondsLeft(String dateTimeStr) {
        try {
            Date targetDate = DATE_TIME_FORMAT.parse(dateTimeStr);
            long timeDiffMillis = targetDate.getTime() - new Date().getTime();
            return timeDiffMillis / 1000;
        }catch (ParseException e){return -1;}
    }

    /**
     * 将秒数转换为日期时间字符串
     * @param seconds 距离当前时间的秒数
     * @return 日期时间字符串，格式为 "dd-MM-yy HH:mm:ss"
     */
    public static String secondsToDateTime(long seconds) {
        Date date = new Date(System.currentTimeMillis() + seconds * 1000L);
        return DATE_TIME_FORMAT.format(date);
    }

    /**
     * 格式化时间差为天、小时、分钟和秒
     * @param seconds 时间差，以秒为单位
     * @return 格式化后的字符串
     */
    public static String formatTimeDifference(long seconds) {
        if (seconds < 0) return "已过期";

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天");
        if (hours > 0 || days > 0) sb.append(hours).append("时");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append("分");
        sb.append(secs).append("秒");

        return sb.toString().trim();
    }
    public static String formatTimeDifference(String dateTimeStr){
        try {
            long seconds = secondsLeft(dateTimeStr);
            return formatTimeDifference(seconds);
        }catch (Exception e){return "已过期";}
    }
    public static String convertSecondsToTime(long totalSeconds) {
        var days = totalSeconds / 86400; // 1天 = 24 * 60 * 60秒
        var hours = (totalSeconds % 86400) / 3600; // 1小时 = 60 * 60秒
        var minutes = (totalSeconds % 3600) / 60; // 1分钟 = 60秒
        var seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(days).append("天");

        if (hours > 0) result.append(hours).append("时");

        if (minutes > 0) result.append(minutes).append("分");

        if (seconds > 0 || result.isEmpty()) result.append(seconds).append("秒"); // 确保即使只有秒也显示

        return result.toString().trim();
    }

    public static boolean isAdmin(Player p){
        return p.isOp();
    }

    public static String getPlayerName(Player p) {
        return p.getName();
    }

    public static List<String> getPlayerNameList(){
        var list = new ArrayList<String>();
        for (var p : nks.getOnlinePlayers().values()) {
            list.add(p.getName());
        }
        return list;
    }

    public static boolean hasClazz(Supplier<Class<?>> classSupplier){
        try {
            classSupplier.get();
            return true;
        } catch (NoClassDefFoundError | Exception e) {
            return false;
        }
    }
    @Getter
    private static final SplittableRandom RANDOM = new SplittableRandom(System.currentTimeMillis());


    //方法名(() -> {方法体})
    public static void MainTask (Runnable logic) {
        nks.getScheduler().scheduleTask(getPlugin(), logic, false);}

    public static void Repeat (Runnable logic,int time,boolean async){
        nks.getScheduler().scheduleRepeatingTask(getPlugin(), logic, time, async);}
    public static void Delayed (Runnable logic,int time){
        Delayed(logic,time,false);}
    public static void Delayed (Runnable logic,int time,boolean async){
        nks.getScheduler().scheduleDelayedTask(getPlugin(), logic ,time, async);}
    public static void Async (Runnable logic){
        nks.getScheduler().scheduleTask(getPlugin(), logic, true);}

    private static final ConsoleCommandSender console = new ConsoleCommandSender();
    public static void asOPCmd(Player p,List<String> cmd){
        cmd.forEach(c -> {if (!c.isEmpty()) asOPCmd(p, c);});
    }
    public static void asOPCmd(Player p,String cmd){
        MainTask(() -> {
            var flag = !cmd.contains("@p") && !p.isOp(); if (flag) p.setOp(true);
            Arrays.asList(cmd.split("&")).forEach(c -> {if (!c.isEmpty()) nks.dispatchCommand(cmd.contains("@p") ? console : p,
                    c.replace("@p", p.getName().contains(" ") ? "\"" + p.getName() + "\"" : p.getName()).replaceFirst("/", ""));});
            if (flag) p.setOp(false);});
    }

    public static String CORE_NAME = "";
    @SuppressWarnings("all")
    public static void checkServer(){
        Async(() -> {
            var ver = false;
            //双核心兼容
            CORE_NAME = "Nukkit";
            try {Class.forName("cn.nukkit.Nukkit").getField("NUKKIT_PM1E");
                ver = true;
                CORE_NAME = "Nukkit PM1E";
            } catch (ClassNotFoundException | NoSuchFieldException ignore) { }
            try {var c = Class.forName("cn.nukkit.Nukkit").getField("NUKKIT");
                CORE_NAME = c.get(c).toString();
                ver = true;
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignore) {}
            AbstractFakeInventory.IS_PM1E = ver;
            if(ver){nks.enableExperimentMode = true;
                nks.forceResources = true;}
        });
    }
    public static Item ChestMenuTo(@NotNull Item item, int Count, int Meta, String Name){
        return ChestMenuTo(item,Count,Meta,Name,false, "");
    }
    public static Item ChestMenuTo(@NotNull Item item,int Count,int Meta,String Name,boolean VisName){
        return ChestMenuTo(item,Count,Meta,Name,VisName, "");
    }
    public static Item ChestMenuTo(@NotNull Item item,int Count,int Meta,String Name,boolean VisName, String Lore){
        item.setCount(Count);
        item.setDamage(Meta);
        item.setCustomName(VisName ? "§r" + Name.replace("&", "§")
                + " " : "§r"+Name.replace("&", "§"));
        item.addEnchantment(Enchantment.get(22).setLevel(32767, false));
        if (!Lore.isEmpty()) item.setLore(Lore.replace("@n", "\n"));
        return item;
    }

    public static Item getItemFromStrId(String strId){
        var iis = strId.split(":");
        try {
            return iis.length > 1 ? Item.get(Integer.parseInt(iis[0]),
                    Integer.parseInt(iis[1])) : Item.get(Integer.parseInt(strId));
        } catch (NumberFormatException e) {
            return switch (iis.length) {
                case 0 -> Item.get(0);
                case 1 -> Item.fromString("minecraft:" + strId);
                case 2 -> {
                    if (iis[1].matches("\\d+")) {
                        var item = Item.fromString("minecraft:" + iis[0]);
                        item.setDamage(Integer.parseInt(iis[1]));
                        yield item;
                    } yield Item.fromString(strId);
                }
                default -> {
                    var item = Item.fromString(iis[0] + ":" + iis[1]);
                    item.setDamage(Integer.parseInt(iis[2]));
                    yield item;
                }
            };
        }
    }

    public static String addCommasIfHasBlanks(String str) {
        return str.contains(" ") ? "\"" + str + "\"" : str;
    }

    public static Player getCmdPlayerByName(CommandSender sender, String argStr) {
        return nks.getPlayer(argStr.replace("@p", sender instanceof Player p ?
                addCommasIfHasBlanks(p.getName()) : argStr));
    }
}
