package cis.matrixclient.event.events.player;

import cis.matrixclient.event.Event;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

public class PlayerMoveEvent extends Event {
    public MovementType type;
    public Vec3d movement;

    public PlayerMoveEvent(MovementType type, Vec3d movement) {
        this.type = type;
        this.movement = movement;
    }
}