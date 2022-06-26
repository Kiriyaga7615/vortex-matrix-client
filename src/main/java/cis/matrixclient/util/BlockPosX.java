package cis.matrixclient.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;

import java.util.ArrayList;
import java.util.List;

import static cis.matrixclient.MatrixClient.mc;

public class BlockPosX extends BlockPos {
    private final List<BlockPosX> blocks = new ArrayList<>();

    public BlockPosX(int i, int j, int k) {
        super(i, j, k);
    }

    public BlockPosX(BlockPos blockPos) {
        super(blockPos);
    }

    // Blocks
    public BlockPos get() {return new BlockPos(this);}
    public boolean isAir() {
        return getState().isAir();
    }
    public boolean isOf(Block block) {
        return getState().isOf(block);
    }
    public boolean isOf(Class klass) {
        return klass.isInstance(this.getState().getBlock());
    }
    public boolean isSolid() {
        return getState().isSolidBlock(mc.world, this);
    }
    public boolean isBlastRes() {
        return getState().getBlock().getBlastResistance() >= 600;
    }
    public boolean isBreakable() {return getState().getHardness(mc.world, this) > 0;}
    public boolean isReplaceable() {return getState().getMaterial().isReplaceable();}
    public BlockState getState() {
        return mc.world.getBlockState(this);
    }
    public void setState(Block block) {
        mc.world.setBlockState(this, block.getDefaultState());
    }
    public List<BlockPosX> directions() {
        blocks.clear();

        for (Direction direction : Direction.values()) {
            blocks.add((BlockPosX) this.offset(direction));
        }

        return blocks;
    }

    // Box
    public Box getBoundingBox() {
        return new Box(this.getX(), this.getY(), this.getZ(), this.getX() + 1, this.getY() + 1, this.getZ() + 1);
    }

    // Vec3d
    public Vec3d getCenter() {
        return new Vec3d(this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5);
    }
    public Vec3d getVec3d() {
        return new Vec3d(this.getX(), this.getY(), this.getZ());
    }
    public Vec3d closestVec3d() {
        return closestVec3d(this.getBoundingBox());
    }
    private Vec3d closestVec3d(Box box) {
        if (box == null) return new Vec3d(0.0, 0.0, 0.0);
        Vec3d eyePos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        double x = MathHelper.clamp(eyePos.getX(), box.minX, box.maxX);
        double y = MathHelper.clamp(eyePos.getY(), box.minY, box.maxY);
        double z = MathHelper.clamp(eyePos.getZ(), box.minZ, box.maxZ);

        return new Vec3d(x, y, z);
    }

    // Distance
    public double distance(BlockPosX blockPosX) {
        return distance(blockPosX.getCenter());
    }
    public double distance(PlayerEntity player) {
        return distance(player.getX() + 0.5, player.getY(), player.getZ() + 0.5);
    }
    public double distance(Vec3d vec3d) {
        return distance(vec3d.x, vec3d.y, vec3d.z);
    }
    public double distance() {
        Vec3d eyePos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        float f = (float) (eyePos.getX() - closestVec3d().x);
        float g = (float) (eyePos.getY() - closestVec3d().y);
        float h = (float) (eyePos.getZ() - closestVec3d().z);
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

    private double distance(double x, double y, double z) {
        float f = (float) (this.getX() - x);
        float g = (float) (this.getY() - y);
        float h = (float) (this.getZ() - z);
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

    // Overriding
    public BlockPosX down() {
        return new BlockPosX(super.down());
    }
    public BlockPosX down(int i) {
        return new BlockPosX(super.down(i));
    }
    public BlockPosX up() {
        return new BlockPosX(super.up());
    }
    public BlockPosX up(int i) {
        return new BlockPosX(super.up(i));
    }
    public BlockPosX north() {
        return new BlockPosX(super.north());
    }
    public BlockPosX north(int i) {
        return new BlockPosX(super.north(i));
    }
    public BlockPosX south() {
        return new BlockPosX(super.south());
    }
    public BlockPosX south(int i) {
        return new BlockPosX(super.south(i));
    }
    public BlockPosX west() {
        return new BlockPosX(super.west());
    }
    public BlockPosX west(int i) {
        return new BlockPosX(super.west(i));
    }
    public BlockPosX east() {
        return new BlockPosX(super.east());
    }
    public BlockPosX east(int i) {
        return new BlockPosX(super.east(i));
    }
    public BlockPosX offset(Direction direction) {
        return new BlockPosX(this.getX() + direction.getOffsetX(), this.getY() + direction.getOffsetY(), this.getZ() + direction.getOffsetZ());
    }
    public BlockPosX offset(Direction direction, int i) {
        return i == 0 ? this : new BlockPosX(this.getX() + direction.getOffsetX() * i, this.getY() + direction.getOffsetY() * i, this.getZ() + direction.getOffsetZ() * i);
    }

    public boolean equals(BlockPosX blockPosX) {
        return this.getX() == blockPosX.getX() && this.getY() == blockPosX.getY() && this.getZ() == blockPosX.getZ();
    }
    public boolean equals(BlockPos blockPos) {
        return this.getX() == blockPos.getX() && this.getY() == blockPos.getY() && this.getZ() == blockPos.getZ();
    }
}
