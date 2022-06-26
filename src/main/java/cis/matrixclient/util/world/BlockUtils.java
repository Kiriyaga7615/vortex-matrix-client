package cis.matrixclient.util.world;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static cis.matrixclient.MatrixClient.mc;

public class BlockUtils {
    public static Block getBlock(BlockPos pos) { return getState(pos).getBlock(); }
    public static boolean isAir(BlockPos pos) { return getState(pos).isAir(); }
    public static BlockState getState(BlockPos pos) { return mc.world.getBlockState(pos); }
    public static boolean isSolid(BlockPos pos) {return getState(pos).isSolidBlock(mc.world, pos);}
    public static boolean isBlastRes(BlockPos pos) {return getBlock(pos).getBlastResistance() >= 600;}

    public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock
                || block instanceof AnvilBlock
                || block instanceof AbstractButtonBlock
                || block instanceof AbstractPressurePlateBlock
                || block instanceof BlockWithEntity
                || block instanceof BedBlock
                || block instanceof FenceGateBlock
                || block instanceof DoorBlock
                || block instanceof NoteBlock
                || block instanceof TrapdoorBlock;
    }

    public static Vec3d vec3d(BlockPos pos) {
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    public static Vec3d bestHitPos(BlockPos pos) {
        if (pos == null) return new Vec3d(0.0, 0.0, 0.0);
        double x = MathHelper.clamp((mc.player.getX() - pos.getX()), 0.0, 1.0);
        double y = MathHelper.clamp((mc.player.getY() - pos.getY()), 0.0, 1.0);
        double z = MathHelper.clamp((mc.player.getZ() - pos.getZ()), 0.0, 1.0);
        return new Vec3d(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }

    public static Vec3d getBlockCenter(BlockPos pos){
        if (pos == null) return new Vec3d(0.0, 0.0, 0.0);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        return new Vec3d(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }

    public static double distance(BlockPos block1, BlockPos block2) {
        double dX = block2.getX() - block1.getX();
        double dY = block2.getY() - block1.getY();
        double dZ = block2.getZ() - block1.getZ();
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distance(Vec3d block1, Vec3d block2) {
        double dX = block2.getX() - block1.getX();
        double dY = block2.getY() - block1.getY();
        double dZ = block2.getZ() - block1.getZ();
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static List<BlockPos> getSphere(BlockPos centerPos, int radius, int height) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (int i = centerPos.getX() - radius; i < centerPos.getX() + radius; i++) {
            for (int j = centerPos.getY() - height; j < centerPos.getY() + height; j++) {
                for (int k = centerPos.getZ() - radius; k < centerPos.getZ() + radius; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    if (distance(centerPos, pos) <= radius && !blocks.contains(pos)) blocks.add(pos);
                }
            }
        }
        return blocks;
    }

    public static List<BlockPos> getSphere(BlockPos pos, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        List<BlockPos> circleblocks = new ArrayList<>();
        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();
        for (int x = cx - (int) r; x <= cx + r; x++) {
            for (int z = cz - (int) r; z <= cz + r; z++) {
                for (int y = (sphere ? cy - (int) r : cy); y < (sphere ? cy + r : cy + h); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

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

    public static List<BlockPos> getSphere(PlayerEntity player, double radiusX, double radiusY) {
        Vec3d eyePos = new Vec3d(player.getX(), player.getY() + player.getEyeHeight(player.getPose()), player.getZ());
        ArrayList<BlockPos> blocks = new ArrayList<>();

        for (double i = eyePos.getX() - radiusX; i < eyePos.getX() + radiusX; i++) {
            for (double j = eyePos.getY() - radiusY; j < eyePos.getY() + radiusY; j++) {
                for (double k = eyePos.getZ() - radiusX; k < eyePos.getZ() + radiusX; k++) {
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

                    if (distance > MathHelper.sqrt((float) (radiusX * radiusX))) continue;
                    BlockPos blockPos = new BlockPos(vecPos);

                    if (blocks.contains(blockPos)) continue;
                    blocks.add(blockPos);
                }
            }
        }

        return blocks;
    }

    public static boolean isHole(BlockPos pos){
        for (Direction direction : Direction.values()){
            if (direction == Direction.UP) continue;
            BlockPos offsetPos = pos.offset(direction);
            if (!isBlastRes(offsetPos)) return false;
        }
        return true;
    }

    public static boolean isSafeHole(BlockPos pos){
        if (!isHole(pos)) return false;
        for (Direction direction : Direction.values()){
            if (direction == Direction.UP) continue;
            Block block = getBlock(pos.offset(direction));
            if (block != Blocks.BEDROCK) return false;
        }
        return true;
    }

    public static boolean isMidSafeHole(BlockPos pos){
        if (!isHole(pos)) return false;
        int bedrock = 0;
        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;
            Block block = getBlock(pos.offset(direction));
            if (block == Blocks.BEDROCK) bedrock++;
        }
        return bedrock != 5;
    }

    public static boolean isDoubleHole(BlockPos pos){
        int air = 0;
        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;
            BlockPos offsetPos = pos.offset(direction);
            if (!isBlastRes(offsetPos)){
                air++;
                for (Direction offsetDir : Direction.values()){
                    if (offsetDir == Direction.UP || offsetDir == direction.getOpposite()) continue;
                    if (!isBlastRes(offsetPos.offset(offsetDir))) return false;
                }
            }
        }
        return air == 1;
    }

    public static Direction getPlaceableSide(BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbour = pos.offset(side);
            BlockState blockState = mc.world.getBlockState(neighbour);
            if (!blockState.getMaterial().isReplaceable()) {
                return side;
            }
        }
        return null;
    }

    public static boolean canBreak(BlockPos pos) {
        return canBreak(pos, mc.world.getBlockState(pos));
    }

    public static boolean canBreak(BlockPos blockPos, BlockState state) {
        if (!mc.player.isCreative() && state.getHardness(mc.world, blockPos) < 0) return false;
        return state.getOutlineShape(mc.world, blockPos) != VoxelShapes.empty();
    }

    public static boolean canPlace(BlockPos blockPos) {
        return canPlace(blockPos, true);
    }

    public static boolean canPlace(BlockPos pos, boolean checkEntities) {
        if (pos == null) return false;
        if (!World.isValid(pos)) return false;
        if (!getState(pos).getMaterial().isReplaceable()) return false;
        return !checkEntities || mc.world.canPlace(getState(pos), pos, ShapeContext.absent());
    }
}
