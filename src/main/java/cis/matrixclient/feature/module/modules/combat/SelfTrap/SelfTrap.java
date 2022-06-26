package cis.matrixclient.feature.module.modules.combat.SelfTrap;

import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.player.FindItemResult;
import cis.matrixclient.util.player.InvUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import static cis.matrixclient.feature.module.modules.combat.SelfTrap.STUtils.*;

@Module.Info(name = "SelfTrap", category = Module.Category.COMBAT)
public class SelfTrap extends Module {
    public enum Mode{
        Full,
        Head,
        Top
    }

    public Setting<Mode> mode = register("Mode", Mode.Top, Mode.values());
    public Setting<Integer> blockPerInterval = register("BlocksPerInterval", 2, 1, 5);
    public Setting<Integer> intervalDelay = register("IntervalDelay", 1, 0, 3);
    public Setting<Boolean> rotate = register("Rotate", false);
    public Setting<Boolean> autoDisable = register("AutoDisable", true);

    private int interval;

    @Override
    public void onEnable() {
        interval = 0;
    }

    @Subscribe
    public void onTick(TickEvent.Post event) {
        if (autoDisable.get() && ((mc.options.jumpKey.isPressed() || mc.player.input.jumping) || mc.player.prevY < mc.player.getPos().getY())) {
            toggle();
            return;
        }

        if (interval > 0) interval--;
        if (interval > 0) return;

        ArrayList<BlockPos> poses = getTrapBlocks(mode);
        FindItemResult block = InvUtils.findInHotbar(Items.OBSIDIAN);
        if (poses.isEmpty() || !block.found() || !isSurrounded()) return;

        for (int i = 0; i <= blockPerInterval.get(); i++) {
            if (poses.size() > i) {
                place(poses.get(i), rotate.get(),block.slot(), true);
            }
        }
        interval = intervalDelay.get();
    }
}