package cis.matrixclient.feature.module.modules.combat.PistonCrystal.positions;

import cis.matrixclient.feature.module.modules.combat.PistonCrystal.PistonCrystal;
import cis.matrixclient.feature.module.modules.combat.PistonCrystal.Triplet;
import cis.matrixclient.util.BlockPosX;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.Comparator;
import java.util.List;

import static cis.matrixclient.MatrixClient.mc;
import static cis.matrixclient.feature.module.modules.combat.PistonCrystal.PCUtils.closestVec3d;
import static cis.matrixclient.feature.module.modules.combat.PistonCrystal.PCUtils.distanceTo;

public class Torch extends Positions {

    @Override
    public List<Triplet> init(PlayerEntity target) {
        positions.clear();
        main = new BlockPosX(target.getBlockPos().up(canUpper(target) ? 2 : 1));

        for (Direction direction : Direction.values()) {
            if (PistonCrystal.instance.strictDirection.get() && mc.player.getHorizontalFacing() != direction) continue;
            if (direction == Direction.UP || direction == Direction.DOWN) continue;
            Direction[] sideDir = sideDirection(direction);

            add(main.offset(direction, 2), main.offset(direction, 3), direction);
            add(main.offset(direction, 2), main.offset(direction, 2).down(), direction);

            add(main.offset(direction, 2), main.offset(direction, 2).offset(sideDir[0]), direction);
            add(main.offset(direction, 2), main.offset(direction, 2).offset(sideDir[1]), direction);

        }

        Comparator<Triplet> comparator = Comparator.comparingDouble(Triplet::distance);
        return positions.stream().sorted(PistonCrystal.instance.distance.get(PistonCrystal.Distance.Closest) ? comparator : comparator.reversed()).toList();
    }

    @Override
    public boolean canPlace(Triplet triplet) {
        if (triplet.extended && !isOpaqueFullCube(triplet.blockPos.get(3))) return false;
        if (torchDirection(triplet.blockPos.get(2)) == Direction.UP) return false;
        if (!canCrystal(triplet.blockPos.get(0))) return false;

        for (BlockPosX blockPos : triplet.blockPos) {
            if (!canPlace(blockPos, false)) return false;
            if (distanceTo(closestVec3d(blockPos.getBoundingBox())) > PistonCrystal.instance.placeRange.get()) return false;
        }

        return true;
    }

    private boolean isOpaqueFullCube(BlockPosX blockPos) {
        return !mc.world.isAir(blockPos) && mc.world.getBlockState(blockPos).isOpaque() && mc.world.getBlockState(blockPos).isFullCube(mc.world, blockPos);
    }

    private boolean isClickable(Block block) {
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

    private Direction torchDirection(BlockPosX blockPos) {
        if (!mc.world.isAir(blockPos.down())) return Direction.DOWN;

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;
            BlockState state = mc.world.getBlockState(blockPos.offset(direction));

            if (state.getBlock() instanceof PistonBlock) continue;
            if (!state.getFluidState().isEmpty()) continue;
            if (isClickable(state.getBlock())) continue;
            if (state.isAir()) continue;

            return direction;
        }

        return Direction.UP;
    }

    private void add(BlockPosX piston, BlockPosX redstone, Direction direction) {
        positions.add(new Triplet(List.of(main.offset(direction), piston, redstone), direction));
    }

    private void add(BlockPosX piston, BlockPosX redstone, BlockPosX opaqueFullCube, Direction direction) {
        Triplet triplet = new Triplet(List.of(main.offset(direction), piston, redstone, opaqueFullCube), direction);
        triplet.extended = true;

        positions.add(triplet);
    }
}
