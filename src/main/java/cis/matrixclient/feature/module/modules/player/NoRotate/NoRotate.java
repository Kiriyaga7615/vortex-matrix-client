package cis.matrixclient.feature.module.modules.player.NoRotate;

import cis.matrixclient.event.events.network.PacketEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.mixins.PlayerPositionLookS2CPacketAccessor;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

@Module.Info(name = "NoRotate", category = Module.Category.PLAYER)
public class NoRotate extends Module {

    @Subscribe
    private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && mc.player != null) {
            ((PlayerPositionLookS2CPacketAccessor) event.packet).setPitch(mc.player.getPitch());
            ((PlayerPositionLookS2CPacketAccessor) event.packet).setYaw(mc.player.getYaw());
        }
    }
}
