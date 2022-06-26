package cis.matrixclient.feature.module.modules.misc;

import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import net.minecraft.item.PickaxeItem;

@Module.Info(name = "NoMiningTrace", category = Module.Category.MISC)
public class NoMiningTrace extends Module {
    public Setting<Boolean> onlyPickaxe = register("OnlyPickaxe", true);

    public boolean canWork() {
        if (!enabled) return false;

        if (onlyPickaxe.get()) return mc.player.getMainHandStack().getItem() instanceof PickaxeItem;
        return true;
    }

    public static NoMiningTrace instance;

    public NoMiningTrace() {
        instance = this;
    }
}