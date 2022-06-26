package cis.matrixclient.feature.module.modules.movement;

import cis.matrixclient.event.events.player.PlayerMoveEvent;
import cis.matrixclient.event.events.player.SetPlayerSprintEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import com.google.common.eventbus.Subscribe;

@Module.Info(name = "Sprint", category = Module.Category.MOVEMENT, drawn = true)
public class Sprint extends Module {
    public enum Mode{
        Normal,
        Rage
    }
    public Setting<Mode> mode = register("Mode", Mode.Normal, Mode.values());

    @Subscribe
    public void onMove(PlayerMoveEvent event) {
        if (mc.player.input.movementForward + mc.player.input.movementSideways != 0)
            mc.player.setSprinting(true);
    }

    @Subscribe
    public void onShit(SetPlayerSprintEvent event) {
        if (mode.get(Mode.Rage) && !mc.player.isSubmergedInWater() && !mc.player.isInLava() && !mc.player.input.jumping)
            event.setSprinting(false).setCancelled(true);
    }

    @Override
    public String getInfo() {
        return mode.get().name();
    }
}
