package cis.matrixclient.event.events.player;

import cis.matrixclient.event.Event;

public class SendMessageEvent extends Event {
    public String message;

    public SendMessageEvent(String message){
        this.setCancelled(false);
        this.message = message;
    }
}
