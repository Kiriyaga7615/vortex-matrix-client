package cis.matrixclient.feature.module.modules.client;

import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.gui.clickgui.AbyssClickGUI;
import cis.matrixclient.feature.gui.clickgui.FutureClickGUI;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.world.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.Screen;

@Module.Info(name = "ClickGUI", category = Module.Category.CLIENT, drawn = false)
public class ClickGUI extends Module {
    public Page abyss = new Page("Abyss");
    public Page future = new Page("Future");

    public enum GUIMode{
        Future,
        Abyss
    }

    public Setting<GUIMode> guiMode = register("GUIMode", GUIMode.Future, GUIMode.values());
    public Setting<Color> futureHighlightColor = register("FutureHighlight", new Color(0.94f, 1f, 0.50f), future);
    public Setting<Color> abyssHighlightColor = register("AbyssHighlightColor", new Color(0.94f, 1f, 0.50f), abyss);
    public Setting<Color> upColor = register("UpColor", new Color(0.94f, 1f, 0.50f, 60), abyss);
    public Setting<Color> downColor = register("DownColor", new Color(0.94f, 1f, 0.50f, 60), abyss);

    private Screen screen;

    @Subscribe
    private void onTick(TickEvent.Post event){
        switch (guiMode.get()){
            case Future -> {
                if (mc.currentScreen instanceof AbyssClickGUI){
                    mc.setScreen(new FutureClickGUI());
                }
            }
            case Abyss -> {
                if (mc.currentScreen instanceof FutureClickGUI){
                    mc.setScreen(new AbyssClickGUI());
                }
            }
        }
    }

    @Override
    public void onEnable() {
        if (!WorldUtils.canUpdate()) return;

        if(mc.currentScreen != null){
            toggle();
            return;
        }

        switch (guiMode.get()){
            case Abyss -> screen = new AbyssClickGUI();
            case Future -> screen = new FutureClickGUI();
        }

        mc.setScreen(screen);
    }
}
