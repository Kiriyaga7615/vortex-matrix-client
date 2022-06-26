package cis.matrixclient.event.events.client;

import cis.matrixclient.event.Event;
import cis.matrixclient.util.key.KeyBinds;

public class MouseButtonEvent extends Event {
    public int button;
    public KeyBinds.Action action;
    public MouseButtonEvent(int button, KeyBinds.Action action){
        this.setCancelled(false);
        this.button = button;
        this.action = action;
    }
}
