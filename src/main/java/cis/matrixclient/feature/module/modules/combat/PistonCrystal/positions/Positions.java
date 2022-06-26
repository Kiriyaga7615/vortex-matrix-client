package cis.matrixclient.feature.module.modules.combat.PistonCrystal.positions;

import cis.matrixclient.feature.module.modules.combat.PistonCrystal.PistonCrystal;
import cis.matrixclient.feature.module.modules.combat.PistonCrystal.Triplet;
import cis.matrixclient.util.BlockPosX;
import net.minecraft.block.Blocks;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

import static cis.matrixclient.MatrixClient.mc;
import static cis.matrixclient.feature.module.modules.combat.PistonCrystal.PCUtils.hasEntity;

public abstract class Positions {
    public final List<Triplet> positions = new ArrayList<>();
    public static BlockPosX main;

    public abstract List<Triplet> init(PlayerEntity target);

    public abstract boolean canPlace(Triplet triplet);

    public boolean canPlace(BlockPosX blockPos, boolean ignoreEntity) {
        if (blockPos == null) return false;
        if (!blockPos.isAir()) return false;
        if (ignoreEntity) return true;

        return !hasEntity(new Box(blockPos), entity -> entity instanceof PlayerEntity || entity instanceof EndCrystalEntity || entity instanceof TntEntity);
    }

    public boolean canCrystal(BlockPosX blockPos) {
        if (hasEntity(new Box(blockPos))) return false;

        return blockPos.isAir() &&
                (blockPos.down().isOf(Blocks.OBSIDIAN) || blockPos.down().isOf(Blocks.BEDROCK));
    }

    public Direction[] sideDirection(Direction direction) {
        if (direction == Direction.WEST || direction == Direction.EAST) {
            return new Direction[]{Direction.NORTH, Direction.SOUTH};
        } else {
            return new Direction[]{Direction.WEST, Direction.EAST};
        }
    }

    public static boolean isUpper() {
        return main.equals(PistonCrystal.instance.target.getBlockPos().up(2)) && !PistonCrystal.instance.canPlace(PistonCrystal.instance.trapPos, true);
    }

    public boolean canUpper(PlayerEntity target) {
        return PistonCrystal.instance.allowUpper.get() && (!facePlace(target) || (PistonCrystal.instance.stacked != null && PistonCrystal.instance.stacked.blockPos.get(0).getY() == target.getBlockPos().getY() + 1 && PistonCrystal.instance.stacked.direction == Direction.UP));
    }

    public boolean facePlace(PlayerEntity target) {
        if (target == null) return false;
        BlockPosX targetPos = new BlockPosX(target.getBlockPos().up());

        for (Direction direction : Direction.values()) {
            if (canCrystal(targetPos.offset(direction))) return true;
        }

        return false;
    }
}
