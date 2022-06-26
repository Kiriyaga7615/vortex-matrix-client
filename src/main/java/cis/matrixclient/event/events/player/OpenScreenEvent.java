package cis.matrixclient.event.events.player;

import cis.matrixclient.event.Event;
import net.minecraft.client.gui.screen.Screen;

public class OpenScreenEvent extends Event {
    public Screen screen;

    public OpenScreenEvent(Screen screen) {
        this.setCancelled(false);
        this.screen = screen;
    }
}