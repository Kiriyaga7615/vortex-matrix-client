package cis.matrixclient.feature.module.modules.misc;

import cis.matrixclient.event.events.player.OpenScreenEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.util.math.BlockPos;

@Module.Info(name = "AutoRespawn", category = Module.Category.MISC, drawn = true)
public class AutoRespawn extends Module {
    public Setting<Boolean> sendCoords = register("SendCoordinates", false);

    private BlockPos pos;

    @Subscribe
    private void onOpenScreen(OpenScreenEvent event){
        pos = mc.player.getBlockPos();
        if (!(event.screen instanceof DeathScreen)) return;

        mc.player.requestRespawn();
        if (sendCoords.get()) info(pos.toShortString());
    }


}
