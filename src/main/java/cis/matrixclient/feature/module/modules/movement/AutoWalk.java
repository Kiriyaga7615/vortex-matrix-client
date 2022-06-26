package cis.matrixclient.feature.module.modules.movement;

import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Input;
import cis.matrixclient.util.key.Keybind;
import cis.matrixclient.util.player.rotations.AdvanceDirection;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.option.KeyBinding;

@Module.Info(name = "AutoWalk", category = Module.Category.MOVEMENT, drawn = true)
public class AutoWalk extends Module {
    public enum Direction {
        Forward, Back, Right, Left
    }

    public Setting<Direction> direction = register("Direction", Direction.Forward, Direction.values());
    public Setting<Boolean> airStop = register("StopOnAir", true);
    public Setting<Boolean> shiftStop = register("StopOnShift", true);
    public Setting<Boolean> strictYaw = register("StrictYaw", true);

    private KeyBinding key;
    AdvanceDirection dir;
    private boolean rotated;

    @Override
    public void onDisable() {
        dir = null;
        key.setPressed(false);
        rotated = false;
    }

    @Subscribe
    public void onTick(TickEvent.Post event) {
        KeyBinding k = getKey();

        if (key != null & key != k & rotated){
            rotated = false;
        }

        if (strictYaw.get()){
            if (dir == null) dir = AdvanceDirection.fromRotation(mc.player.getYaw());
            if (!rotated){
                dir = rotate(dir);
            }

            mc.player.setYaw(dir.asRotation());
            rotated = true;
        }



        key = k;
        if (key == null) return;
        key.setPressed(!onAir() && !onShift());
    }

    private AdvanceDirection rotate(AdvanceDirection dir){
        switch (direction.get()){
            case Back -> dir = dir.getOpposite();
            case Right -> dir = dir.getCounterClockWise();
            case Left -> dir = dir.getClockWise();
        }
        return dir;
    }

    private KeyBinding getKey(){
        switch (direction.get()) {
            case Forward -> {
                return mc.options.forwardKey;
            }
            case Back -> {
                return mc.options.backKey;
            }
            case Right -> {
                return mc.options.rightKey;
            }
            case Left -> {
                return mc.options.leftKey;
            }
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    private boolean onAir() {
        return airStop.get() && mc.player.fallDistance > 2;
    }

    private boolean onShift() {
        return shiftStop.get() && mc.player.isSneaking();
    }
}