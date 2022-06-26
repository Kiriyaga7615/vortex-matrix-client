package cis.matrixclient.feature.module.modules.movement.speed;

import cis.matrixclient.event.events.network.PacketEvent;
import cis.matrixclient.event.events.player.PlayerMoveEvent;
import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.world.Timer;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.interfaces.IVec3d;
import cis.matrixclient.util.math.TimerUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec2f;

import static cis.matrixclient.feature.module.modules.movement.speed.SUtils.*;

@Module.Info(name = "Speed", category = Module.Category.MOVEMENT)
public class Speed extends Module {
    public Setting<Double> timer = register("Timer", 1, 0.5, 3, 1);
    public Setting<Boolean> whenWater = register("WhenWater", false);
    public Setting<Boolean> whenLava = register("WhenLava", true);
    public Setting<Boolean> whenSneaking = register("WhenSneaking", false);
    public Setting<Double> factor = register("Factor", 1.8, 1, 4, 1);
    public Setting<Boolean> AutoJump = register("AutoJump", true);
    public Setting<Boolean> limit = register("Limit", false);
    public Setting<Boolean> pause = register("Pause", false);

    private int ac;
    private double distance;
    private double ffactor;
    private long l = 0L;


    @Override
    public void onEnable() {
        reset();
    }

    @Override
    public void onDisable() {
        Timer.instance.setOverride(Timer.OFF);
    }

    @Subscribe
    private void onPlayerMove(PlayerMoveEvent event) {
        if (event.type != MovementType.SELF
                //|| pause.get() && (ElytraFly.get.isActive() || Jesus.get.isActive())
                || mc.player.isFallFlying()
                || mc.player.isInSwimmingPose()
                || mc.player.isClimbing()
                || !whenSneaking.get() && mc.player.isSneaking()
                || !whenWater.get() && mc.player.isTouchingWater()
                || !whenLava.get() && mc.player.isInLava())
            return;

        Timer.instance.setOverride(isPlayerMoving() ? timer.get().floatValue() : 1);

        switch (ac) {
            case 0: {
                if (isPlayerMoving()) {
                    ac++;
                    ffactor = (double) 1.18f * getDefaultSpeed() - 0.01;
                }
            }
            case 1: {
                if (!isPlayerMoving() || !mc.player.isOnGround() || !AutoJump.get())
                    break;
                ((IVec3d) event.movement).setY(GetPlayerHeight(0.40123128));
                ffactor *= factor.get();
                ac++;
                break;
            }
            case 2: {
                ffactor = distance - 0.76 * (distance - getDefaultSpeed());
                ac++;
                break;
            }
            case 3: {
                if (!mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().y, 0.0)) || mc.player.verticalCollision && ac > 0) {
                    ac = 0;
                }
                ffactor = distance - distance / 159.0;
            }
        }
        ffactor = Math.max(ffactor, getDefaultSpeed());


        if (limit.get()) {
            if (System.currentTimeMillis() - l > 2500L) {
                l = System.currentTimeMillis();
            }
            this.ffactor = Math.min(this.ffactor, System.currentTimeMillis() - l > 1250L ? 0.44 : 0.43);
        }

        Vec2f change = OmniSpeeds(ffactor);
        double velX = change.x;
        double velZ = change.y;
        ((IVec3d) event.movement).setXZ(velX, velZ);
    }


    @Subscribe
    private void onPreTick(TickEvent.Pre event) {
        if (mc.player.isFallFlying()
                || mc.player.isInSwimmingPose()
                || mc.player.isClimbing()
                || !whenSneaking.get() && mc.player.isSneaking()
                || !whenWater.get() && mc.player.isTouchingWater()
                || !whenLava.get() && mc.player.isInLava())
            return;

        distance = Math.sqrt((mc.player.getX() - mc.player.prevX) * (mc.player.getX() - mc.player.prevX) + (mc.player.getZ() - mc.player.prevZ) * (mc.player.getZ() - mc.player.prevZ));
    }

    @Override
    public String getInfo() {
        return limit.get()? "Strict" : "Strafe";
    }

    @Subscribe
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            reset();
        }
    }

    private void reset() {
        ac = 0;
        distance = 0.0;
        ffactor = 0.2873;
    }
}