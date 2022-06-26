package cis.matrixclient.event.events.world;

import cis.matrixclient.event.Event;
import net.minecraft.entity.Entity;

public class EntityAddedEvent extends Event {

    public Entity entity;

    public EntityAddedEvent(Entity entity) {
        this.entity = entity;
    }

}
