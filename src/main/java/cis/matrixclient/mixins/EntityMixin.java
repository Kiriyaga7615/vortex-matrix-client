package cis.matrixclient.mixins;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.player.PlayerMoveEvent;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.movement.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static cis.matrixclient.MatrixClient.mc;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "move", at = @At("HEAD"))
    private void onMove(MovementType type, Vec3d movement, CallbackInfo info) {
        if ((Object) this != MinecraftClient.getInstance().player) return;

        PlayerMoveEvent event = new PlayerMoveEvent(type, movement);
        event.type = type;
        event.movement = movement;
        MatrixClient.EVENT_BUS.post(event);
    }

    @ModifyArgs(method = "pushAwayFrom(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void onPushAwayFrom(Args args, Entity entity) {
        Velocity velocity = ModuleManager.getModule(Velocity.class);

        if ((Object) this == mc.player && velocity.isEnabled() && velocity.push.get()) {
            args.set(0, (double) args.get(0) * 0);
            args.set(2, (double) args.get(2) * 0);
        }
    }
}
