package cis.matrixclient.mixins;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.world.EntityAddedEvent;
import cis.matrixclient.event.events.world.EntityRemovedEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import static cis.matrixclient.MatrixClient.mc;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Shadow
    @Nullable
    public abstract Entity getEntityById(int id);

    @Inject(method = "addEntityPrivate", at = @At("TAIL"))
    private void onSpawnEntityPrivate(int id, Entity entity, CallbackInfo info) {
        if (entity != null) MatrixClient.EVENT_BUS.post(new EntityAddedEvent(entity));
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void onRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo info) {
        if (mc.world != null && mc.player != null && getEntityById(entityId) != null)
            MatrixClient.EVENT_BUS.post(new EntityRemovedEvent(getEntityById(entityId)));

    }
}