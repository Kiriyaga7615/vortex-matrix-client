package cis.matrixclient.mixins;

import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.render.NoRender;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void render(CallbackInfo info) {
		if (ModuleManager.getModule(NoRender.class).bossbar.get()) info.cancel();
	}
}