package cis.matrixclient.mixins;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.client.MouseButtonEvent;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.render.Freecam;
import cis.matrixclient.util.key.Input;
import cis.matrixclient.util.key.KeyBinds;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

@Mixin(value = Mouse.class, priority = 1200)
public class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo info) {
        Input.setButtonState(button, action != GLFW_RELEASE);
        MouseButtonEvent mouseButtonEvent = new MouseButtonEvent(button, KeyBinds.Action.get(action));
        MatrixClient.EVENT_BUS.post(mouseButtonEvent);
        if (mouseButtonEvent.isCancelled()) info.cancel();
    }

    @Redirect(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private void updateMouseChangeLookDirection(ClientPlayerEntity player, double cursorDeltaX, double cursorDeltaY) {
        Freecam freecam = ModuleManager.getModule(Freecam.class);

        if (freecam.isEnabled()) freecam.changeLookDirection(cursorDeltaX * 0.15, cursorDeltaY * 0.15);
        else player.changeLookDirection(cursorDeltaX, cursorDeltaY);
    }
}