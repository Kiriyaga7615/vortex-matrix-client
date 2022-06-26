package cis.matrixclient.mixins;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.network.PacketEvent;
import cis.matrixclient.util.world.WorldUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo info) {
        if (!WorldUtils.canUpdate()) return;
        PacketEvent.Receive receive = new PacketEvent.Receive(packet);
        MatrixClient.EVENT_BUS.post(receive);
        if (receive.isCancelled()) info.cancel();
    }

    @Inject(at = @At("HEAD"), method = "send(Lnet/minecraft/network/Packet;)V", cancellable = true)
    private void onSendPacketHead(Packet<?> packet, CallbackInfo info) {
        if (!WorldUtils.canUpdate()) return;

        PacketEvent.Send send = new PacketEvent.Send(packet);
        MatrixClient.EVENT_BUS.post(send);
        if (send.isCancelled()) info.cancel();
    }

    @Inject(method = "send(Lnet/minecraft/network/Packet;)V", at = @At("TAIL"))
    private void onSendPacketTail(Packet<?> packet, CallbackInfo info) {
        if (!WorldUtils.canUpdate()) return;
        MatrixClient.EVENT_BUS.post(new PacketEvent.Sent(packet));
    }
}