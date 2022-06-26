package cis.matrixclient.mixins;

import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {

	@Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
	private static void onRenderFireOverlay(MinecraftClient minecraftClient, MatrixStack matrixStack, CallbackInfo ci) {
		if (ModuleManager.getModule(NoRender.class).fire.get()) ci.cancel();

	}

	@Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
	private static void onRenderUnderwaterOverlay(MinecraftClient minecraftClient, MatrixStack matrixStack, CallbackInfo ci) {
		if (ModuleManager.getModule(NoRender.class).water.get()) ci.cancel();
	}

	@Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderInWall(Sprite sprite, MatrixStack matrixStack, CallbackInfo ci) {
		NoRender module = ModuleManager.getModule(NoRender.class);
		if (module.wall.get() && module.isEnabled()) ci.cancel();
	}
}