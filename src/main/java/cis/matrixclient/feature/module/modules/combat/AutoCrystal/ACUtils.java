package cis.matrixclient.feature.module.modules.combat.AutoCrystal;

import cis.matrixclient.mixins.MinecraftClientAccessor;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static cis.matrixclient.MatrixClient.mc;

public class ACUtils {
    public static List<BlockPos> getSphere(PlayerEntity player, double radius) {
        Vec3d eyePos = new Vec3d(player.getX(), player.getY() + player.getEyeHeight(player.getPose()), player.getZ());
        ArrayList<BlockPos> blocks = new ArrayList<>();

        for (double i = eyePos.getX() - radius; i < eyePos.getX() + radius; i++) {
            for (double j = eyePos.getY() - radius; j < eyePos.getY() + radius; j++) {
                for (double k = eyePos.getZ() - radius; k < eyePos.getZ() + radius; k++) {
                    Vec3d vecPos = new Vec3d(i, j, k);

                    // Closest Vec3d
                    double x = MathHelper.clamp(eyePos.getX() - vecPos.getX(), 0.0, 1.0);
                    double y = MathHelper.clamp(eyePos.getY() - vecPos.getY(), 0.0, 1.0);
                    double z = MathHelper.clamp(eyePos.getZ() - vecPos.getZ(), 0.0, 1.0);
                    Vec3d vec3d = new Vec3d(eyePos.getX() + x, eyePos.getY() + y, eyePos.getZ() + z);

                    // Distance to Vec3d
                    float f = (float) (eyePos.getX() - vec3d.x);
                    float g = (float) (eyePos.getY() - vec3d.y);
                    float h = (float) (eyePos.getZ() - vec3d.z);

                    double distance = MathHelper.sqrt(f * f + g * g + h * h);

                    if (distance > radius) continue;
                    BlockPos blockPos = new BlockPos(vecPos);

                    if (blocks.contains(blockPos)) continue;
                    blocks.add(blockPos);
                }
            }
        }

        return blocks;
    }

    public static boolean hasEntity(Box box) {
        return hasEntity(box, entity -> entity instanceof PlayerEntity || entity instanceof EndCrystalEntity || entity instanceof TntEntity || entity instanceof ItemEntity);
    }

    public static boolean hasEntity(Box box, Predicate<Entity> predicate) {
        return !mc.world.getOtherEntities(null, box, predicate).isEmpty();
    }

    public static EndCrystalEntity getEntity(BlockPos blockPos) {
        if (blockPos == null) return null;

        return hasEntity(new Box(blockPos), entity -> entity instanceof EndCrystalEntity) ?
                (EndCrystalEntity) mc.world.getOtherEntities(null, new Box(blockPos), entity -> entity instanceof EndCrystalEntity).get(0) : null;
    }

    public  static Vec3d roundVec(Entity entity) {
        BlockPos blockPos = entity.getBlockPos().down();

        return new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5);
    }

    public static Vec3d roundVec(BlockPos blockPos) {
        return new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 0.5);
    }

    public static double distanceTo(BlockPos blockPos) {
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

    public static Vec3d closestVec3d(Box box) {
        if (box == null) return new Vec3d(0.0, 0.0, 0.0);
        Vec3d eyePos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        double x = MathHelper.clamp(eyePos.getX(), box.minX, box.maxX);
        double y = MathHelper.clamp(eyePos.getY(), box.minY, box.maxY);
        double z = MathHelper.clamp(eyePos.getZ(), box.minZ, box.maxZ);

        return new Vec3d(x, y, z);
    }

    public static void processAttack(Entity entity, AutoCrystal.Attack attack) {
        switch (attack) {
            case Packet -> mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
            case Client -> mc.player.attack(entity);
            case Vanilla -> {
                HitResult prevResult = mc.crosshairTarget;

                mc.crosshairTarget = new EntityHitResult(entity, closestVec3d(entity.getBoundingBox()));
                leftClick();
                mc.crosshairTarget = prevResult;
            }
        }
    }

    public static void leftClick() {
        mc.options.attackKey.setPressed(true);
        ((MinecraftClientAccessor) mc).leftClick();
        mc.options.attackKey.setPressed(false);
    }

    public static List<BlockPos> getSurroundBlocks(PlayerEntity player, boolean allBlocks) {
        ArrayList<BlockPos> positions = new ArrayList<>();
        List<Entity> getEntityBoxes;

        for (BlockPos blockPos : getSphere(player.getBlockPos(), 3, 1)) {
            if (!allBlocks && !mc.world.getBlockState(blockPos).getMaterial().isReplaceable()) continue;
            getEntityBoxes = mc.world.getOtherEntities(null, new Box(blockPos), entity -> entity == player);
            if (!getEntityBoxes.isEmpty()) continue;

            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP || direction == Direction.DOWN) continue;

                getEntityBoxes = mc.world.getOtherEntities(null, new Box(blockPos.offset(direction)), entity -> entity == player);
                if (!getEntityBoxes.isEmpty()) positions.add(blockPos);
            }
        }

        return positions;
    }

    public static boolean isSurrounded(PlayerEntity player) {
        return getSurroundBlocks(player, false).isEmpty();
    }

    public static boolean isFaceTrapped() {
        PlayerEntity player = mc.player;
        ArrayList<BlockPos> positions = new ArrayList<>();

        for (BlockPos blockPos : getSphere(player.getBlockPos().up(2), 3, 1)) {
            if (!mc.world.isAir(blockPos)) continue;

            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP || direction == Direction.DOWN) continue;

                positions.add(blockPos);
            }
        }

        return positions.isEmpty();
    }

    public static List<BlockPos> getSphere(BlockPos centerPos, int radius, int height) {
        ArrayList<BlockPos> blocks = new ArrayList<>();

        for (int i = centerPos.getX() - radius; i < centerPos.getX() + radius; i++) {
            for (int j = centerPos.getY() - height; j < centerPos.getY() + height; j++) {
                for (int k = centerPos.getZ() - radius; k < centerPos.getZ() + radius; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (centerPos.getSquaredDistance(pos) <= radius && !blocks.contains(pos)) blocks.add(pos);
                }
            }
        }

        return blocks;
    }

    public static int getWorstArmor(PlayerEntity e) {
        List<ItemStack> list = Lists.newArrayList(e.getItemsEquipped());
        if (list.isEmpty()) return 0;
        int bigger = 0;
        for (ItemStack stack : list) {
            int i = Math.round(((stack.getMaxDamage() - stack.getDamage()) * 100f) / (float) stack.getMaxDamage());
            if (i > bigger) bigger = i;
        }

        return bigger;
    }
}
