package cis.matrixclient.feature.module.modules.render.CustomFOV;

import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import com.google.common.eventbus.Subscribe;

@Module.Info(name = "CustomFov", category = Module.Category.RENDER)
public class CustomFOV extends Module {
    public Setting<Integer> fov = register("Fov", 109, 60, 110);

    @Subscribe
    public void onTick(TickEvent.Post event) {
        mc.options.getFov().setValue(fov.get());
    }
}
