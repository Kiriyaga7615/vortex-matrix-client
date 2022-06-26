package cis.matrixclient.feature.module.modules.combat.PistonCrystal;

import cis.matrixclient.mixins.MinecraftClientAccessor;
import cis.matrixclient.util.BlockPosX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static cis.matrixclient.MatrixClient.mc;

public class PCUtils {

    public static double distanceTo(BlockPosX blockPos) {
        return distanceTo(closestVec3d(new Box(blockPos)));
    }

    public static double distanceTo(Vec3d vec3d) {
        return distanceTo(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    public static double distanceTo(double x, double y, double z) {
        Vec3d eyePos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        float f = (float) (eyePos.getX() - x);
        float g = (float) (eyePos.getY() - y);
        float h = (float) (eyePos.getZ() - z);
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

    public static boolean hasEntity(Box box) {
        return hasEntity(box, entity -> entity instanceof PlayerEntity || entity instanceof EndCrystalEntity || entity instanceof TntEntity || entity instanceof ItemEntity);
    }

    public static boolean hasEntity(Box box, Predicate<Entity> predicate) {
        return !mc.world.getOtherEntities(null, box, predicate).isEmpty();
    }

    public static void leftClick() {
        mc.options.attackKey.setPressed(true);
        ((MinecraftClientAccessor) mc).leftClick();
        mc.options.attackKey.setPressed(false);
    }

    public static Vec3d closestVec3d(Box box) {
        if (box == null) return new Vec3d(0.0, 0.0, 0.0);

        double x = MathHelper.clamp(mc.player.getX(), box.minX, box.maxX);
        double y = MathHelper.clamp(mc.player.getY(), box.minY, box.maxY);
        double z = MathHelper.clamp(mc.player.getZ(), box.minZ, box.maxZ);

        return new Vec3d(x, y, z);
    }

    public static int getYaw(Direction direction) {
        if (direction == null) return (int) mc.player.getYaw(1.0F);

        return switch (direction) {
            case NORTH -> 180;
            case SOUTH -> 0;
            case WEST -> 90;
            case EAST -> -90;
            default -> (int) mc.player.getYaw(1.0F);
        };
    }
}