package cis.matrixclient.event.events.client;

import cis.matrixclient.event.Event;

public class MouseScrollEvent extends Event {
    public double value;

    public MouseScrollEvent(double value) {
        this.setCancelled(false);
        this.value = value;
    }
}