package cis.matrixclient.mixins;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.player.StartBreakingBlockEvent;
import cis.matrixclient.interfaces.IClientPlayerInteractionManager;
import cis.matrixclient.util.world.WorldUtils;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin implements IClientPlayerInteractionManager {
    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void onAttackBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        if (!WorldUtils.canUpdate()) return;
        StartBreakingBlockEvent event = new StartBreakingBlockEvent(blockPos, direction);
        MatrixClient.EVENT_BUS.post(event);
        if (event.isCancelled()) info.cancel();
    }

    @Shadow
    protected abstract void syncSelectedSlot();

    @Override
    public void syncSelected() {
        syncSelectedSlot();
    }
}