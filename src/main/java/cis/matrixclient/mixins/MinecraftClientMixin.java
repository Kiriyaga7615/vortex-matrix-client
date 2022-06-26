package cis.matrixclient.mixins;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.player.OpenScreenEvent;
import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.gui.GuiScreen;
import cis.matrixclient.feature.manager.ConfigManager;
import cis.matrixclient.util.world.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Final
    public Mouse mouse;


    @Unique
    private long lastTime;

    @Unique
    private boolean firstFrame;

    @Shadow @Final private Window window;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        MatrixClient.INSTANCE.onInitialize();
        firstFrame = true;
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void onPreTick(CallbackInfo info) {
        if (!WorldUtils.canUpdate()) return;
        MatrixClient.EVENT_BUS.post(new TickEvent.Pre());
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo info) {
        if (!WorldUtils.canUpdate()) return;
        MatrixClient.EVENT_BUS.post(new TickEvent.Post());
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof GuiScreen) screen.mouseMoved(mouse.getX() * window.getScaleFactor(), mouse.getY() * window.getScaleFactor());
        if (!WorldUtils.canUpdate()) return;
        OpenScreenEvent event = new OpenScreenEvent(screen);
        MatrixClient.EVENT_BUS.post(event);

        if (event.isCancelled()) info.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(CallbackInfo info) {
        long time = System.currentTimeMillis();

        if (firstFrame) {
            lastTime = time;
            firstFrame = false;
        }

        WorldUtils.frameTime = (time - lastTime) / 1000.0;
        lastTime = time;
    }


    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V"))
    public void crash(CallbackInfo info) {
        ConfigManager configManager = new ConfigManager();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void stop(CallbackInfo info) {
        ConfigManager configManager = new ConfigManager();
    }
}