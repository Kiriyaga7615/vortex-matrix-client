package cis.matrixclient.util.player;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.network.SendMovementPacketsEvent;
import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.util.misc.Pool;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import static cis.matrixclient.MatrixClient.mc;

public class Rotations {
    private static final Pool<Rotation> rotationPool = new Pool<>(Rotation::new);
    private static final List<Rotation> rotations = new ArrayList<>();
    public static float serverYaw;
    public static float serverPitch;
    public static int rotationTimer;
    private static float preYaw, prePitch;
    private static int i = 0;

    private static Rotation lastRotation;
    private static int lastRotationTimer;
    private static boolean sentLastRotation;
    public static boolean rotating = false;

    public static void init() {
        MatrixClient.EVENT_BUS.register(new Rotations());
    }

    public static void rotate(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
        Rotation rotation = rotationPool.get();
        rotation.set(yaw, pitch, priority, clientSide, callback);

        int i = 0;
        for (; i < rotations.size(); i++) {
            if (priority > rotations.get(i).priority) break;
        }

        rotations.add(i, rotation);
    }

    public static void rotate(double yaw, double pitch, int priority, Runnable callback) {
        rotate(yaw, pitch, priority, false, callback);
    }

    public static void rotate(double yaw, double pitch, Runnable callback) {
        rotate(yaw, pitch, 0, callback);
    }

    public static void rotate(double yaw, double pitch) {
        rotate(yaw, pitch, 0, null);
    }

    private static void resetLastRotation() {
        if (lastRotation != null) {
            rotationPool.free(lastRotation);

            lastRotation = null;
            lastRotationTimer = 0;
        }
    }

    @Subscribe
    private static void onSendMovementPacketsPre(SendMovementPacketsEvent.Pre event) {
        if (mc.cameraEntity != mc.player) return;
        sentLastRotation = false;

        if (!rotations.isEmpty()) {
            rotating = true;
            resetLastRotation();

            Rotation rotation = rotations.get(i);
            setupMovementPacketRotation(rotation);

            if (rotations.size() > 1) rotationPool.free(rotation);

            i++;
        } else if (lastRotation != null) {
            if (lastRotationTimer >= 4) {
                resetLastRotation();
                rotating = false;
            } else {
                setupMovementPacketRotation(lastRotation);
                sentLastRotation = true;

                lastRotationTimer++;
            }
        }
    }

    private static void setupMovementPacketRotation(Rotation rotation) {
        setClientRotation(rotation);
        setCamRotation(rotation.yaw, rotation.pitch);
    }

    private static void setClientRotation(Rotation rotation) {
        preYaw = mc.player.getYaw();
        prePitch = mc.player.getPitch();

        mc.player.setYaw((float) rotation.yaw);
        mc.player.setPitch((float) rotation.pitch);
    }

    @Subscribe
    private static void onSendMovementPacketsPost(SendMovementPacketsEvent.Post event) {
        if (!rotations.isEmpty()) {
            if (mc.cameraEntity == mc.player) {
                rotations.get(i - 1).runCallback();

                if (rotations.size() == 1) lastRotation = rotations.get(i - 1);

                resetPreRotation();
            }

            for (; i < rotations.size(); i++) {
                Rotation rotation = rotations.get(i);

                setCamRotation(rotation.yaw, rotation.pitch);
                if (rotation.clientSide) setClientRotation(rotation);
                rotation.sendPacket();
                if (rotation.clientSide) resetPreRotation();

                if (i == rotations.size() - 1) lastRotation = rotation;
                else rotationPool.free(rotation);
            }

            rotations.clear();
            i = 0;
        } else if (sentLastRotation) {
            resetPreRotation();
        }
    }

    private static void resetPreRotation() {
        mc.player.setYaw(preYaw);
        mc.player.setPitch(prePitch);
    }

    @Subscribe
    private static void onTick(TickEvent.Pre event) {
        rotationTimer++;
    }

    public static double getYaw(Entity entity) {
        return mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(entity.getZ() - mc.player.getZ(), entity.getX() - mc.player.getX())) - 90f - mc.player.getYaw());
    }

    public static double getYaw(Vec3d pos) {
        return mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(pos.getZ() - mc.player.getZ(), pos.getX() - mc.player.getX())) - 90f - mc.player.getYaw());
    }

    public static double getPitch(Vec3d pos) {
        double diffX = pos.getX() - mc.player.getX();
        double diffY = pos.getY() - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = pos.getZ() - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getPitch());
    }


    public static double getYaw(BlockPos pos) {
        return mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(pos.getZ() + 0.5 - mc.player.getZ(), pos.getX() + 0.5 - mc.player.getX())) - 90f - mc.player.getYaw());
    }

    public static double getPitch(BlockPos pos) {
        double diffX = pos.getX() + 0.5 - mc.player.getX();
        double diffY = pos.getY() + 0.5 - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = pos.getZ() + 0.5 - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getPitch());
    }

    public static Vec2f getPitchYaw(Vec3d targetV3) {
        double vec = 57.2957763671875;
        Vec3d target = targetV3.subtract(mc.player.getEyePos());
        double square = Math.sqrt(target.x * target.x + target.z * target.z);
        float pitch = MathHelper.wrapDegrees((float) (-(MathHelper.atan2(target.y, square) * vec)));
        float yaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(target.z, target.x) * vec) - 90.0F);
        return new Vec2f(pitch, yaw);
    }

    public static void setCamRotation(double yaw, double pitch) {
        serverYaw = (float) yaw;
        serverPitch = (float) pitch;
        rotationTimer = 0;
    }

    private static class Rotation {
        public double yaw, pitch;
        public int priority;
        public boolean clientSide;
        public Runnable callback;

        public void set(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.priority = priority;
            this.clientSide = clientSide;
            this.callback = callback;
        }

        public void sendPacket() {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround((float) yaw, (float) pitch, mc.player.isOnGround()));
            runCallback();
        }

        public void runCallback() {
            if (callback != null) callback.run();
        }
    }
}

