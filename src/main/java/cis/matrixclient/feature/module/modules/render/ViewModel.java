package cis.matrixclient.feature.module.modules.render;

import cis.matrixclient.event.events.render.HeldItemRendererEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3f;

import java.util.List;

@Module.Info(name = "ViewModel", category = Module.Category.RENDER)
public class ViewModel extends Module {
    public enum SwingMode{
        Offhand,
        Mainhand,
        None
    }

    // Scale
    public Setting<Double> scaleMain = register("Scale Main", 1, 0, 2, 2);
    public Setting<Double> scaleOff = register("Scale Off", 1, 0, 2, 2);

    // Position
    public Setting<Double> posX = register("Pos X", 1, -2, 2, 2);
    public Setting<Double> posY = register("Pos Y", -0.4, -2, 2, 2);
    public Setting<Double> posZ = register("Pos Z", -1, -2, 2, 2);

    // Rotation
    public Setting<Double> rotationX = register("Rotate X", 0, -180, 180, 0);
    public Setting<Double> rotationY = register("Rotate Y", 0, -180, 180, 0);
    public Setting<Double> rotationZ = register("Rotate Z", 0, -180, 180, 0);

    // Eat
    public Setting<Double> useXMain = register("Main Use X", 0, -2, 2, 2);
    public Setting<Double> useZMain = register("Main Use Z", 0, -2, 2, 2);
    public Setting<Double> useXOff = register("Off Use X", 0, -2, 2, 2);
    public Setting<Double> useZOff = register("Off Use Z", 0, -2, 2, 2);

    // Swing
    public Setting<SwingMode> swingMode = register("Swing", SwingMode.None, SwingMode.values());

    @Subscribe
    public void onHand(HeldItemRendererEvent.Hand event) {
        if (!enabled) return;

        if (event.hand == Hand.MAIN_HAND) {
            event.matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(rotationX.get().floatValue()));
            event.matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationY.get().floatValue()));
            event.matrix.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationZ.get().floatValue()));
            event.matrix.scale(scaleMain.get().floatValue(), scaleMain.get().floatValue(), scaleMain.get().floatValue());
            if (mc.player.isUsingItem() && mc.player.getMainHandStack().getItem().isFood()) {
                event.matrix.translate(posX.get().floatValue() - posX.get().floatValue() + useXMain.get().floatValue(), posY.get().floatValue() -0.6D, posZ.get().floatValue() - posZ.get().floatValue() + useZMain.get().floatValue());
            } else {
                event.matrix.translate(posX.get().floatValue(), posY.get().floatValue(), posZ.get().floatValue());
            }
        } else {
            event.matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(rotationX.get().floatValue()));
            event.matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rotationY.get().floatValue()));
            event.matrix.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotationZ.get().floatValue()));
            event.matrix.scale(scaleOff.get().floatValue(), scaleOff.get().floatValue(), scaleOff.get().floatValue());
            if (mc.player.isUsingItem() && mc.player.getOffHandStack().getItem().isFood()) {
                event.matrix.translate(-posX.get().floatValue() - useXOff.get().floatValue(), posY.get().floatValue() - 0.6D, posZ.get().floatValue() - posZ.get().floatValue() - useZOff.get().floatValue());
            } else {
                event.matrix.translate(-posX.get().floatValue(), posY.get().floatValue(), posZ.get().floatValue());
            }
        }
    }

    // отрезать еврею письку handview by pank
    public static ViewModel get;

    public ViewModel() {
        get = this;
    }
}