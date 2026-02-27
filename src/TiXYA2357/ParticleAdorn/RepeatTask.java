package TiXYA2357.ParticleAdorn;

import cn.nukkit.scheduler.PluginTask;

import static TiXYA2357.ParticleAdorn.Configs.PT;
import static TiXYA2357.ParticleAdorn.Main.*;
import static TiXYA2357.ParticleAdorn.Utils.secondsLeft;

public class RepeatTask extends PluginTask<Main> {
    public RepeatTask(Main owner) {
        super(owner);
    }
    @Override
    public void onRun(int i) {
        nks.getOnlinePlayers().values().forEach(p -> getPlayerParticles(p).entrySet().removeIf(e -> {
            var tt = e.getValue().equals("永久") ? 1 : secondsLeft(e.getValue());
            if (tt < 1) p.sendMessage(PT + "你的" + title + "§f " + e.getKey() + " §r§7已过期");
            return tt < 1;
        }));

    }
}
