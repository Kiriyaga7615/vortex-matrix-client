package cis.matrixclient.mixins;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.game.ChangePerspectiveEvent;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.render.Freecam;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @Inject(method = "setPerspective", at = @At("HEAD"), cancellable = true)
    private void setPerspective(Perspective perspective, CallbackInfo info) {
        ChangePerspectiveEvent event = new ChangePerspectiveEvent(perspective);

        MatrixClient.EVENT_BUS.post(event);

        if (event.isCancelled()) info.cancel();

        if (ModuleManager.getModule(Freecam.class).isEnabled()) info.cancel();
    }
}