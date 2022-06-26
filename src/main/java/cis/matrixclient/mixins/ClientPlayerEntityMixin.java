package cis.matrixclient.mixins;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.network.SendMovementPacketsEvent;
import cis.matrixclient.event.events.player.SendMessageEvent;
import cis.matrixclient.event.events.player.SetPlayerSprintEvent;
import cis.matrixclient.feature.command.CommandManager;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.movement.NoSlow;
import cis.matrixclient.feature.module.modules.movement.Velocity;
import cis.matrixclient.feature.module.modules.player.PortalChat;
import cis.matrixclient.util.player.ChatUtils;
import cis.matrixclient.util.world.WorldUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile, PlayerPublicKey publicKey) {
        super(world, profile, publicKey);
    }

    private boolean ignoreChatMessage;

    @Shadow
    public abstract void sendChatMessage(String string);


    @Inject(method = "sendChatMessage(Ljava/lang/String;Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, Text preview, CallbackInfo info) {
        if (!WorldUtils.canUpdate()) return;

        if (ignoreChatMessage) return;
        String prefix = CommandManager.getPrefix();
        if (!message.startsWith(prefix) && !message.startsWith("/")) {
            SendMessageEvent event = new SendMessageEvent(message);
            MatrixClient.EVENT_BUS.post(event);

            if (!event.isCancelled()) {
                ignoreChatMessage = true;
                sendChatMessage(event.message);
                ignoreChatMessage = false;
            }

            info.cancel();
            return;
        }

        if (message.startsWith(prefix)){
            try {
                CommandManager.dispatch(message.substring(prefix.length()));
            } catch (CommandSyntaxException e) {
                ChatUtils.error(e.getMessage());
            }
            info.cancel();
        }
    }

    @Redirect(method = "updateNausea", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;inNetherPortal:Z", ordinal = 0))
    public boolean portalChat(ClientPlayerEntity clientPlayerEntity) {
        return ((EntityAccessor) clientPlayerEntity).isInNetherPortal() && !ModuleManager.getModule(PortalChat.class).enabled;
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    public void onSetSprint(boolean sprinting, CallbackInfo ci) {
        SetPlayerSprintEvent event = new SetPlayerSprintEvent(sprinting);
        MatrixClient.EVENT_BUS.post(event);

        if(event.isCancelled()) {
            event.setSprinting(event.isSprinting());
            ci.cancel();
        }
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(double x, double d, CallbackInfo info) {
        if (Velocity.get.enabled && Velocity.get.push.get()) {
            info.cancel();
        }
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean redirectUsingItem(ClientPlayerEntity player) {
        if (ModuleManager.getModule(NoSlow.class).enabled) return false;
        return player.isUsingItem();
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    private void onSendMovementPacketsHead(CallbackInfo info) {
        MatrixClient.EVENT_BUS.post(new SendMovementPacketsEvent.Pre());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 0))
    private void onTickHasVehicleBeforeSendPackets(CallbackInfo info) {
        MatrixClient.EVENT_BUS.post(new SendMovementPacketsEvent.Pre());
    }

    @Inject(method = "sendMovementPackets", at = @At("TAIL"))
    private void onSendMovementPacketsTail(CallbackInfo info) {
        MatrixClient.EVENT_BUS.post(new  SendMovementPacketsEvent.Post());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void onTickHasVehicleAfterSendPackets(CallbackInfo info) {
        MatrixClient.EVENT_BUS.post(new  SendMovementPacketsEvent.Post());
    }
}