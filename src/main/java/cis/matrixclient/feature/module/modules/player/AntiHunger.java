package cis.matrixclient.feature.module.modules.player;

import cis.matrixclient.event.events.network.PacketEvent;
import cis.matrixclient.feature.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

@Module.Info(name = "AntiHunger", category = Module.Category.PLAYER, drawn = true)
public class AntiHunger extends Module {

    @Subscribe
    private void onPacket(PacketEvent.Send event){
        Packet<?> packet = event.packet;

        if (packet instanceof ClientCommandC2SPacket clientCommandC2SPacket && (clientCommandC2SPacket.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING || clientCommandC2SPacket.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING)){
            event.cancel();
        }
    }
}
