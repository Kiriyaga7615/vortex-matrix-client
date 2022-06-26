package cis.matrixclient.util.player.rotations;

import cis.matrixclient.util.math.MathUtils;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

import java.util.Arrays;
import java.util.Comparator;

import static cis.matrixclient.MatrixClient.mc;

public enum AdvanceDirection {
    NORTH(0, 4, 2, 6, "north", -180),
    NORTH_EAST(1, 5, 3, 7, "north_east", -135),
    EAST(2, 6, 4, 0, "east",-90),
    SOUTH_EAST(3, 7, 5, 1,"south_east", -45),
    SOUTH(4, 0, 6, 2, "south", 0),
    SOUTH_WEST(5, 1, 7, 3, "south_west", 45),
    WEST(6, 2, 0, 4, "west",90),
    NORTH_WEST(7, 3, 1, 5, "north_west", 135);

    private final int id;
    private final int oppositeId;
    private final int clockWiseId;
    private final int counterClockWiseId;
    private final String name;
    private final float yaw;

    AdvanceDirection(int id, int oppositeId, int clockWiseId, int counterClockWiseId, String name, float yaw){
        this.id = id;
        this.oppositeId = oppositeId;
        this.clockWiseId = clockWiseId;
        this.counterClockWiseId = counterClockWiseId;
        this.name = name;
        this.yaw = yaw;
    }

    private static final AdvanceDirection[] ALL = values();
    private static final AdvanceDirection[] VALUES = Arrays.stream(ALL).sorted(Comparator.comparingInt((direction) -> direction.id)).toArray(AdvanceDirection[]::new);

    public static AdvanceDirection fromRotation(float yaw){
        for (int i = 0; i < AdvanceDirection.values().length; i++){
            if(AdvanceDirection.values()[i].yaw == MathUtils.findClosest(getYaws(), yaw)) return AdvanceDirection.values()[i];
        }
        return null;
    }

    public float asRotation() { return this.yaw; }

    public AdvanceDirection getOpposite() {
        return byId(this.oppositeId);
    }

    public AdvanceDirection getClockWise(){
        return byId(this.clockWiseId);
    }

    public AdvanceDirection getCounterClockWise(){
        return byId(this.counterClockWiseId);
    }

    public static AdvanceDirection byId(int id) {
        return VALUES[MathHelper.abs(id % VALUES.length)];
    }

    public static float[] getYaws(){
        float[] array = new float[8];
        for (int i = 0; i < AdvanceDirection.values().length; i++){
            array[i] = AdvanceDirection.values()[i].yaw;
        }
        return array;
    }

    public String getName() {
        return this.name;
    }
}
