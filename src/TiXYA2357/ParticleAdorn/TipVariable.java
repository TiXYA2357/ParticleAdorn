package TiXYA2357.ParticleAdorn;

import cn.nukkit.Player;
import tip.utils.Api;
import tip.utils.variables.BaseVariable;

import static TiXYA2357.ParticleAdorn.Main.AdornParticle_TipVal;
import static TiXYA2357.ParticleAdorn.Main.getPlayerUseParticleIsReplaceName;

public class TipVariable extends BaseVariable {

    public TipVariable(Player player, String str) {
        super(player);
        this.string = str;
    }

    public static void init() {
        Api.registerVariables("ParticleAdorn",TipVariable.class);
    }
    @Override
    public void strReplace() {
        addStrReplaceString(AdornParticle_TipVal, getPlayerUseParticleIsReplaceName(player));
    }
}
