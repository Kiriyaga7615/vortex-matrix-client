package cis.matrixclient.feature.module.modules.combat.PistonCrystal;

import cis.matrixclient.util.BlockPosX;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.List;

import static cis.matrixclient.feature.module.modules.combat.PistonCrystal.PCUtils.closestVec3d;
import static cis.matrixclient.feature.module.modules.combat.PistonCrystal.PCUtils.distanceTo;

public class Triplet {
    public List<BlockPosX> blockPos;
    public Direction direction;
    public boolean extended;

    public Triplet(List<BlockPosX> blockPos, Direction direction) {
        this.blockPos = blockPos;
        this.direction = direction;
        this.extended = false;
    }

    public double distance() {
        return distanceTo(closestVec3d(blockPos.get(2).getBoundingBox()));
    }
}