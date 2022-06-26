package cis.matrixclient.event.events.player;

import cis.matrixclient.event.Event;

public class SetPlayerSprintEvent extends Event {
    public boolean sprinting;

    public SetPlayerSprintEvent(boolean sprinting){
        this.sprinting = sprinting;
    }

    public SetPlayerSprintEvent setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
        return this;
    }

    public boolean isSprinting() { return sprinting; }
}
