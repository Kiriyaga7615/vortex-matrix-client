package cis.matrixclient.event.events.network;

import cis.matrixclient.event.Event;
import net.minecraft.network.Packet;

public class PacketEvent extends Event {
    public Packet<?> packet;

    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            this.setCancelled(false);
            this.packet = packet;
        }
    }

    public static class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            this.setCancelled(false);
            this.packet = packet;
        }
    }

    public static class Sent extends PacketEvent {
        public Sent(Packet<?> packet) {
            this.packet = packet;
        }
    }
}
