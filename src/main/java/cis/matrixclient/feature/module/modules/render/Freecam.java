package cis.matrixclient.feature.module.modules.render;

import cis.matrixclient.event.events.client.KeyEvent;
import cis.matrixclient.event.events.client.MouseScrollEvent;
import cis.matrixclient.event.events.game.GameLeftEvent;
import cis.matrixclient.event.events.player.OpenScreenEvent;
import cis.matrixclient.event.events.world.ChunkOcclusionEvent;
import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Input;
import cis.matrixclient.util.key.KeyBinds;
import cis.matrixclient.util.math.Vec3;
import cis.matrixclient.util.player.Rotations;
import cis.matrixclient.util.world.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

@Module.Info(name = "Freecam", category = Module.Category.RENDER, drawn = true)
public class Freecam extends Module {
    public Setting<Double> speed = register("Speed", 1, 0f, 10f, 2);
    public Setting<Double> scroll = register("Scroll", 0f, 0f, 2f, 2);
    public Setting<Boolean> rotate = register("Rotate", false);

    public final Vec3 pos = new Vec3();
    public final Vec3 prevPos = new Vec3();

    private Perspective perspective;
    private double speedValue;

    public float yaw, pitch;
    public float prevYaw, prevPitch;

    private boolean forward, backward, right, left, up, down;

    @Override
    public void onEnable() {
        if (!WorldUtils.canUpdate()) return;
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        perspective = mc.options.getPerspective();
        speedValue = speed.get();

        pos.set(mc.gameRenderer.getCamera().getPos());
        prevPos.set(mc.gameRenderer.getCamera().getPos());

        prevYaw = yaw;
        prevPitch = pitch;

        forward = false;
        backward = false;
        right = false;
        left = false;
        up = false;
        down = false;

        unpress();
    }

    @Override
    public void onDisable() {
        if (!WorldUtils.canUpdate()) return;
        mc.options.setPerspective(perspective);
    }

    private void unpress() {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
    }

    @Subscribe
    private void onOpenScreen(OpenScreenEvent event){
        unpress();

        prevPos.set(pos);
        prevYaw = yaw;
        prevPitch = pitch;
    }

    @Subscribe
    private void onTick(TickEvent.Post event){
        if (perspective == null || mc.options.getPerspective() == null) return;
        if (mc.cameraEntity.isInsideWall()) mc.getCameraEntity().noClip = true;
        if (!perspective.isFirstPerson()) mc.options.setPerspective(Perspective.FIRST_PERSON);

        Vec3d forward = Vec3d.fromPolar(0, yaw);
        Vec3d right = Vec3d.fromPolar(0, yaw + 90);
        double velX = 0;
        double velY = 0;
        double velZ = 0;

        if (rotate.get()) {
            BlockPos crossHairPos;
            Vec3d crossHairPosition;

            if (mc.crosshairTarget instanceof EntityHitResult) {
                crossHairPos = ((EntityHitResult) mc.crosshairTarget).getEntity().getBlockPos();
                Rotations.rotate(Rotations.getYaw(crossHairPos), Rotations.getPitch(crossHairPos), 0, null);
            } else {
                crossHairPosition = mc.crosshairTarget.getPos();
                crossHairPos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();

                if (!mc.world.getBlockState(crossHairPos).isAir()) {
                    Rotations.rotate(Rotations.getYaw(crossHairPosition), Rotations.getPitch(crossHairPosition), 0, null);
                }
            }
        }

        double s = 0.5;
        if (mc.options.sprintKey.isPressed()) s = 1;

        boolean a = false;
        if (this.forward) {
            velX += forward.x * s * speedValue;
            velZ += forward.z * s * speedValue;
            a = true;
        }
        if (this.backward) {
            velX -= forward.x * s * speedValue;
            velZ -= forward.z * s * speedValue;
            a = true;
        }

        boolean b = false;
        if (this.right) {
            velX += right.x * s * speedValue;
            velZ += right.z * s * speedValue;
            b = true;
        }
        if (this.left) {
            velX -= right.x * s * speedValue;
            velZ -= right.z * s * speedValue;
            b = true;
        }

        if (a && b) {
            double diagonal = 1 / Math.sqrt(2);
            velX *= diagonal;
            velZ *= diagonal;
        }

        if (this.up) {
            velY += s * speedValue;
        }
        if (this.down) {
            velY -= s * speedValue;
        }

        prevPos.set(pos);
        pos.set(pos.x + velX, pos.y + velY, pos.z + velZ);
    }

    @Subscribe
    private void onKey(KeyEvent event){
        if (Input.isKeyPressed(GLFW.GLFW_KEY_F3)) return;

        boolean cancel = true;

        if (mc.options.forwardKey.matchesKey(event.key, 0) || mc.options.forwardKey.matchesMouse(event.key)) {
            forward = event.action != KeyBinds.Action.RELEASE;
            mc.options.forwardKey.setPressed(false);
        }
        else if (mc.options.backKey.matchesKey(event.key, 0) || mc.options.backKey.matchesMouse(event.key)) {
            backward = event.action != KeyBinds.Action.RELEASE;
            mc.options.backKey.setPressed(false);
        }
        else if (mc.options.rightKey.matchesKey(event.key, 0) || mc.options.rightKey.matchesMouse(event.key)) {
            right = event.action != KeyBinds.Action.RELEASE;
            mc.options.rightKey.setPressed(false);
        }
        else if (mc.options.leftKey.matchesKey(event.key, 0) || mc.options.leftKey.matchesMouse(event.key)) {
            left = event.action != KeyBinds.Action.RELEASE;
            mc.options.leftKey.setPressed(false);
        }
        else if (mc.options.jumpKey.matchesKey(event.key, 0) || mc.options.jumpKey.matchesMouse(event.key)) {
            up = event.action != KeyBinds.Action.RELEASE;
            mc.options.jumpKey.setPressed(false);
        }
        else if (mc.options.sneakKey.matchesKey(event.key, 0) || mc.options.sneakKey.matchesMouse(event.key)) {
            down = event.action != KeyBinds.Action.RELEASE;
            mc.options.sneakKey.setPressed(false);
        }
        else {
            cancel = false;
        }

        if (cancel) event.cancel();
    }

    @Subscribe
    private void onScroll(MouseScrollEvent event){
        if (scroll.get() > 0) {
            speedValue += event.value * 0.25 * (scroll.get() * speedValue);
            if (speedValue < 0.1) speedValue = 0.1;

            event.cancel();
        }
    }

    @Subscribe
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @Subscribe
    private void onGameLeft(GameLeftEvent event) {
        toggle();
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        prevYaw = yaw;
        prevPitch = pitch;

        yaw += deltaX;
        pitch += deltaY;

        pitch = MathHelper.clamp(pitch, -90, 90);
    }

    public double getX(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.x, pos.x);
    }
    public double getY(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.y, pos.y);
    }
    public double getZ(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.z, pos.z);
    }

    public double getYaw(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevYaw, yaw);
    }
    public double getPitch(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPitch, pitch);
    }
}
