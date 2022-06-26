package cis.matrixclient.event.events.game;

import cis.matrixclient.event.Event;
import net.minecraft.client.option.Perspective;

public class ChangePerspectiveEvent extends Event {
    public Perspective perspective;

    public ChangePerspectiveEvent(Perspective perspective) {
        this.setCancelled(false);
        this.perspective = perspective;
    }
}