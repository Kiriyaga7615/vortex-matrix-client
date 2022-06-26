package cis.matrixclient.event.events.world;

import cis.matrixclient.event.Event;

public class ChunkOcclusionEvent extends Event {
    public ChunkOcclusionEvent() {
        this.setCancelled(false);
    }
}