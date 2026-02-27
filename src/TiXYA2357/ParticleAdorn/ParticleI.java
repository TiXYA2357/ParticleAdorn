package TiXYA2357.ParticleAdorn;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.network.protocol.SpawnParticleEffectPacket;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static TiXYA2357.ParticleAdorn.Configs.PT;
import static TiXYA2357.ParticleAdorn.Main.getPlayerUseParticle;
import static TiXYA2357.ParticleAdorn.Main.*;
import static TiXYA2357.ParticleAdorn.PlayerI.getOrDefaultPlayerI;
import static TiXYA2357.ParticleAdorn.SimpleCalculatorWithParentheses.calculate;
import static java.lang.Math.*;

public class ParticleI {

    // 性能评估等级, 如果该值大于玩家自己设置的值则不显示给此玩家
    int performanceEvaluation = 0;

    // 循环任务调度的间隔(tick), 作用于画圆角度增长速度
    int schedulingIntervals;
    // 换帧间隔
    int frameInterval;
    // 读帧延迟
    int readInterval;

    // k轴增量, 垂直
    int kDelta;
    // v轴增量, 水平
    int vDelta;


    // k轴的值(1 ~ 180)
    int kAxisValue;
    // v轴的值(1 ~ 360)
    int vAxisValue;

    // 静态预计算表
    private List<HashMap<String, List<String>>> PosList = new ArrayList<>();
    // 运动算法表
    private List<HashMap<String, List<HashMap<String, String>>>> MoveList = new ArrayList<>();

    // 记录读取到哪帧了
    int locPI = 0;
    int movPI = 0;

    private int si = 0;
    private int fi = 0;
    private int ri = 0;


    // 文件名称: 粒子信息
    public static final HashMap<String, ParticleI> ParticleList = new HashMap<>();

    static void init() {
        ParticleList.clear();

        var path = new File(getConfigPath() + "/Particles");
        if (!path.exists() && !path.mkdirs()) {
            nks.getLogger().error(PT + "无法创建目录: " + path);
            return;
        }
        for (var file : Objects.requireNonNull(path.listFiles())) {
            if (file.isDirectory()) continue;
            var config = new Config(path + "/" + file.getName(), Config.YAML);
            var particle = new ParticleI();
            particle.performanceEvaluation = min(5, max(1, config.get("性能评估等级", 1)));
            particle.schedulingIntervals = max(0, config.get("调度间隔", 1));
            particle.frameInterval = max(0, config.get("换帧间隔", 20));
            particle.readInterval = max(0, config.get("读帧延迟", 1));
            particle.kDelta = max(1, config.get("k轴增量", 1));
            particle.vDelta = max(1, config.get("v轴增量", 1));
            particle.PosList = config.get("静态预算表", new ArrayList<>());
            particle.MoveList = config.get("动态算法表", new ArrayList<>());
            ParticleList.put(file.getName().replace(".yml", ""), particle);
        }
        if (executors != null) executors.shutdown();
        if (executors != null && executors.isShutdown()) {
            executors = Executors.newScheduledThreadPool(1);
            executors.scheduleAtFixedRate(() -> {try {
                schedule.run();
            } catch (Exception e) {
                executors.shutdown();
                nks.getLogger().error(PT + "执行粒子系统任务调度器时出现错误:\n", e);
            }}, 0, 50, TimeUnit.MILLISECONDS);
        }
    }

    static ScheduledExecutorService executors = Executors.newScheduledThreadPool(1);

    static Runnable schedule = () -> {
        if (ParticleList.isEmpty()) return;
        ParticleList.values().forEach(ParticleI::updateDelta);
        nks.getOnlinePlayers().values().forEach(p -> {
            if (getOrDefaultPlayerI(p.getUniqueId() + "")
                    .performanceOptimizationLevel > 0) { // 性能优化0级直接不显示了

                var useParticle = getPlayerUseParticle(p);
                if (invts.containsKey(useParticle) && !useParticle.equals(def_use)) {
                    ((List<String>) invts.get(useParticle)
                            .getOrDefault("粒子", Collections.emptyList())).forEach(particle -> {

                        var particleI = ParticleList.get(particle);
                        if (particleI != null) {
                            if (particleI.readInterval > 0) particleI.ri ++;
                            if (particleI.readInterval == 0 || particleI.ri % particleI.readInterval == 0) {
                                if (particleI.ri > particleI.readInterval) particleI.ri = 0;

                                particleI.PosList.get(particleI.locPI).forEach((k, v) -> {
                                    for (var loc : v) {
                                        var pk = new SpawnParticleEffectPacket();
                                        pk.dimensionId = p.getLevel().getDimension();
                                        pk.identifier = k;
                                        var locs = loc.split(":");
                                        var absPos = new Position(
                                                Double.parseDouble(locs[0]),
                                                Double.parseDouble(locs[1]),
                                                Double.parseDouble(locs[2])
                                        );
                                        var yaw = Math.toRadians(p.getYaw());
                                        pk.position = p.getPosition().add(
                                                absPos.x * Math.cos(yaw) - absPos.z * Math.sin(yaw),
                                                p.getEyeHeight() + absPos.y,
                                                absPos.x * Math.sin(yaw) + absPos.z * Math.cos(yaw)
                                        ).asVector3f();
                                        p.getLevel().getPlayers().values().forEach(pl -> {
                                            var lv = getOrDefaultPlayerI(pl.getUniqueId() + "")
                                                    .performanceOptimizationLevel;
                                            if (lv > 0 && particleI.performanceEvaluation <= lv) pl.dataPacket(pk);
                                        });
                                    }
                                });

                                particleI.MoveList.get(particleI.movPI).forEach((k, v) -> {
                                    for (var loc : v) {
                                        var pk = new SpawnParticleEffectPacket();
                                        pk.dimensionId = p.getLevel().getDimension();
                                        pk.identifier = k;
                                        pk.position = particleI.getPosByMap(loc, p).asVector3f();
                                        p.getLevel().getPlayers().values().forEach(pl -> {
                                            var lv = getOrDefaultPlayerI(pl.getUniqueId() + "")
                                                    .performanceOptimizationLevel;
                                            if (lv > 0 && particleI.performanceEvaluation <= lv) pl.dataPacket(pk);
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    };


    String replaceI(String str, Player p){
        return str
                .replace("x", p.x + "")
                .replace("y", p.y + "")
                .replace("z", p.z + "")
                .replace("yaw", p.getYaw() + "")
                .replace("pitch", p.getPitch() + "")
                .replace("eh", p.getEyeHeight() + "")
                .replace("k", this.kAxisValue + "")
                .replace("v", this.vAxisValue + "");
    }

    Position getPosByMap(HashMap<String, String> Posmap, Player p) {
//        var absPos = new Position(
//                calculate(replaceI(Posmap.getOrDefault("x", p.getFloorX() + ""), p)),
//                calculate(replaceI(Posmap.getOrDefault("y", p.getFloorY() + ""), p)),
//                calculate(replaceI(Posmap.getOrDefault("z", p.getFloorZ() + ""), p))
//        );
//        var yaw = Math.toRadians(p.getYaw());
        return p.getPosition().add(

                calculate(replaceI(Posmap.getOrDefault("x", "0"), p)),
                calculate(replaceI(Posmap.getOrDefault("y", "0"), p)),
                calculate(replaceI(Posmap.getOrDefault("z", "0"), p))
//                absPos.x * Math.cos(yaw) - absPos.z * Math.sin(yaw),
//                p.getEyeHeight() + absPos.y,
//                absPos.x * Math.sin(yaw) + absPos.z * Math.cos(yaw)
        );
    }

    // 在任务调度器中应该是0tick延迟
    public void updateDelta() {
        if (schedulingIntervals > 0) si ++;
        if (frameInterval > 0) fi ++;

        if (schedulingIntervals == 0 || si % schedulingIntervals == 0) {
            if (si > schedulingIntervals) si= 0;
            kAxisValue = (kAxisValue + kDelta - 1) % 180 + 1;
            vAxisValue = (vAxisValue + vDelta - 1) % 360 + 1;
            if (kAxisValue < 1) kAxisValue = 1;
            if (vAxisValue < 1) vAxisValue = 1;
        }
        if (frameInterval == 0 || fi % frameInterval == 0) {
            if (fi > frameInterval) fi = 0;
            if (PosList.size() > 1 && locPI < PosList.size() -1) locPI ++;
            else locPI = 0;
            if (MoveList.size() > 1 && movPI < MoveList.size() -1) movPI ++;
            else movPI = 0;
        }
    }
}
