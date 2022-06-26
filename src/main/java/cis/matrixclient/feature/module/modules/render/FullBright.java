package cis.matrixclient.feature.module.modules.render;

import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.mixins.StatusEffectInstanceAccessor;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potions;

@Module.Info(name = "FullBright", category = Module.Category.RENDER, drawn = true)
public class FullBright extends Module {
    public enum Mode{
        Gamma,
        Effect
    }

    public Setting<Mode> mode = register("Mode", Mode.Gamma, Mode.values());

    public Setting<Integer> gammaValue = register("GammaValue", 4, 0, 4);

    @Override
    public void onDisable() {
        if (mode.get() == Mode.Effect) mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }

    @Subscribe
    private void onTick(TickEvent.Post event){
        switch (mode.get()){
            case Effect -> {
                if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                    StatusEffectInstance statusEffectInstance = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
                    ((StatusEffectInstanceAccessor) statusEffectInstance).setDuration(5800);
                }
                else mc.player.addStatusEffect(new StatusEffectInstance(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 5800, 1)));
            }
            case Gamma -> {
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        }

    }
}
