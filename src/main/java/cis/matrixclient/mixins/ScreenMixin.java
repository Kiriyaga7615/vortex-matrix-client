package cis.matrixclient.mixins;

import cis.matrixclient.feature.gui.GuiScreen;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.client.ClickGUI;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import static cis.matrixclient.MatrixClient.mc;

@Mixin(Screen.class)
public class ScreenMixin {
    private ClickGUI clickGUI = ModuleManager.getModule(ClickGUI.class);

    @ModifyConstant(method = "renderBackground(Lnet/minecraft/client/util/math/MatrixStack;I)V", constant = @Constant(intValue = -1072689136))
    private int startColor(int c) {
        if (mc.currentScreen instanceof GuiScreen){
            return clickGUI.upColor.get().getRgb();
        }
        return 0;
    }

    @ModifyConstant(method = "renderBackground(Lnet/minecraft/client/util/math/MatrixStack;I)V", constant = @Constant(intValue = -804253680))
    private int endColor(int c) {
        if (mc.currentScreen instanceof GuiScreen){
            return clickGUI.downColor.get().getRgb();
        }
        return 0;
    }
}
