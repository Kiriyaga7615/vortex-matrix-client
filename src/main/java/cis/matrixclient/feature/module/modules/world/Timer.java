package cis.matrixclient.feature.module.modules.world;

import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.misc.NoMiningTrace;
import cis.matrixclient.feature.setting.Setting;

@Module.Info(name = "Timer", category = Module.Category.WORLD, drawn = true)
public class Timer extends Module {
    public Setting<Double> multiplier = register("Multiplier", 1, 0.1, 10, 1);

    public static final double OFF = 1;
    private double override = 1;

    public double getMultiplier() {
        return override != OFF ? override : (isEnabled() ? multiplier.get() : OFF);
    }

    public void setOverride(double override) {
        this.override = override;
    }

    public static Timer instance;

    public Timer() {
        instance = this;
    }
}
