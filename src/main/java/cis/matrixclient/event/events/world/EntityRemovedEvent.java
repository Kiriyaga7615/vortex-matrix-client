package cis.matrixclient.event.events.world;

import cis.matrixclient.event.Event;
import net.minecraft.entity.Entity;

public class EntityRemovedEvent extends Event {
    public Entity entity;

    public EntityRemovedEvent(Entity entity) {
        this.entity = entity;
    }
}
