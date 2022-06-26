package cis.matrixclient.mixins;

import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.render.FullBright;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V"))
    private void update(Args args) {
        FullBright fullBright = ModuleManager.getModule(FullBright.class);
        if (fullBright.enabled && fullBright.mode.get() == FullBright.Mode.Gamma) {
            args.set(2, 0xFFFFFFFF);
        }
    }
}