package cis.matrixclient.feature.module.modules.player.PingSpoof;

import cis.matrixclient.event.events.network.PacketEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Module.Info(name = "PingSpoof", category = Module.Category.PLAYER)
public class PingSpoof extends Module {
    public Setting<Integer> ping = register("Ping", 100, 100, 2000);

    private final Executor packetManager = Executors.newSingleThreadExecutor();
    private final List<Packet<?>> packets = new ArrayList<>();
    private boolean sendingPackets;
    private boolean sendPackets;

    @Override
    public void onEnable() {
        sendingPackets = false;
        sendPackets = false;

        packetManager.execute(this::managePackets);
    }

    private void managePackets() {
        while (this.isEnabled()) {
            try {
                Thread.sleep(ping.get());
            } catch (Exception ignored) {}
            sendPackets = true;
        }
    }

    @Subscribe
    private void onSendPacket(PacketEvent.Send event) {
        if (sendingPackets) return;
        if (event.packet instanceof KeepAliveC2SPacket) {
            packets.add(event.packet);
            event.cancel();
        }

        if (sendPackets) {
            sendingPackets = true;
            packets.forEach(packet -> {
                try {
                    mc.getNetworkHandler().sendPacket(packet);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            });
            sendingPackets = false;
            packets.clear();
            sendPackets = false;
        }
    }
}
