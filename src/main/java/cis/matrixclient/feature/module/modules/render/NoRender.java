package cis.matrixclient.feature.module.modules.render;

import cis.matrixclient.event.events.world.ParticleEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import com.google.common.eventbus.Subscribe;
import net.minecraft.particle.ParticleTypes;

@Module.Info(name = "NoRender", category = Module.Category.RENDER, drawn = true)
public class NoRender extends Module {
    public Page particlesPage = new Page("Particles");
    public Page overlaysPage = new Page("Overlays");
    public Page overlaysPage2 = new Page("Overlays2");
    public Page effectsPage = new Page("Effects");

    public Setting<Boolean> explosion = register("Explosion", true, particlesPage);
    public Setting<Boolean> totemParticles = register("TotemParticles", true, particlesPage);
    public Setting<Boolean> firework = register("Firework", true, particlesPage);
    public Setting<Boolean> smoke = register("Smoke", true, particlesPage);
    public Setting<Boolean> effect = register("Effect", true, particlesPage);

    public Setting<Boolean> fog = register("Fog", true, overlaysPage);
    public Setting<Boolean> pumpkin = register("Pumpkin", true, overlaysPage);
    public Setting<Boolean> vignette = register("Vignette", true, overlaysPage);
    public Setting<Boolean> spyglass = register("Spyglass", true, overlaysPage);
    public Setting<Boolean> hurtcam = register("Hurtcam", true, overlaysPage);
    public Setting<Boolean> bossbar = register("Bossbar", true, overlaysPage);
    public Setting<Boolean> gui = register("Gui", true, overlaysPage);
    public Setting<Boolean> weather = register("Weather", true, overlaysPage);

    public Setting<Boolean> totem = register("Totem", true, overlaysPage2);
    public Setting<Boolean> icons = register("Icons", true, overlaysPage2);
    public Setting<Boolean> portal = register("Portal", true, overlaysPage2);
    public Setting<Boolean> scoreboard = register("Scoreboard", true, overlaysPage2);
    public Setting<Boolean> froze = register("Froze", true, overlaysPage2);
    public Setting<Boolean> fire = register("Fire", true, overlaysPage2);
    public Setting<Boolean> water = register("Water", true, overlaysPage2);
    public Setting<Boolean> wall = register("Wall", true, overlaysPage2);

    public Setting<Boolean> nausea = register("Nausea", true, effectsPage);
    public Setting<Boolean> blindness = register("Blindness", true, effectsPage);

    @Subscribe
    private void onParticle(ParticleEvent event){
        if (explosion.get() && event.particle == ParticleTypes.EXPLOSION) event.cancel();
        if (totemParticles.get() && event.particle == ParticleTypes.TOTEM_OF_UNDYING) event.cancel();
        if (firework.get() && event.particle == ParticleTypes.FIREWORK) event.cancel();
        if (effect.get() && event.particle == ParticleTypes.ENTITY_EFFECT) event.cancel();
        if (smoke.get() && (event.particle == ParticleTypes.SMOKE || event.particle == ParticleTypes.LARGE_SMOKE || event.particle == ParticleTypes.CAMPFIRE_COSY_SMOKE || event.particle == ParticleTypes.CAMPFIRE_SIGNAL_SMOKE)) event.cancel();;
    }
}
