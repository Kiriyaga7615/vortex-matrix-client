package cis.matrixclient.feature.gui.components.abyss;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.feature.command.CommandManager;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.client.ClickGUI;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Input;
import cis.matrixclient.util.key.KeyBinds;
import cis.matrixclient.util.key.Keybind;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.render.font.FontRenderers;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

import java.awt.*;


public class AbyssSettingFrame {
    public final Setting<?> setting;
    public Page page;

    private int height;
    private int mouseX, mouseY;
    private boolean lClick, expanded;

    private boolean listening;

    private final ClickGUI clickGUI = ModuleManager.getModule(ClickGUI.class);

    public AbyssSettingFrame(Page page, Setting<?> setting) {
        this.page = page;
        this.setting = setting;
    }

    public int getHeight(){
        return height;
    }

    @SuppressWarnings("unchecked")
    public void render(MatrixStack matrixStack, int x, int y, int width, int height, int mouseX, int mouseY, boolean lClick, boolean rClick, boolean drag) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.lClick = lClick;

        if (setting.getType() == Setting.Type.Boolean){
            this.height = height;

            if(Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y + 1, width, height - 1)) && lClick){
                ((Setting<Boolean>)setting).setValue(!((Setting<Boolean>)setting).get());
            }

            boolean enabled = ((Setting<Boolean>)setting).get();

            DrawableHelper.fill(matrixStack, x, y, x + width, y + height, enabled ? clickGUI.abyssHighlightColor.get().getRgb() : new Color(0, 0, 0.04f).getRgb());
            FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
            FontRenderers.getCustomNormal(14).drawString(matrixStack, setting.getName(), x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
        }
        if (setting.getType() == Setting.Type.Integer){
            this.height = height;

            float start = ((Setting<Integer>)setting).get() - ((Setting<Integer>)setting).getMin();
            float perc = start / (((Setting<Integer>)setting).getMax() - ((Setting<Integer>)setting).getMin());
            float pos = perc * width;

            if (Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && drag){
                float position = mouseX - x + (50 / (((Setting<Integer>)setting).getMax() - ((Setting<Integer>)setting).getMin()));
                float percent = MathHelper.clamp(position / width, 0, 1);
                int value = ((Setting<Integer>)setting).getMin() + (int) (percent * (((Setting<Integer>)setting).getMax() - ((Setting<Integer>)setting).getMin()));
                ((Setting<Integer>)setting).setValue(Math.round(value));
            }

            DrawableHelper.fill(matrixStack, x, y, (int) (x + pos), y + height, clickGUI.abyssHighlightColor.get().getRgb());
            DrawableHelper.fill(matrixStack, (int) (x + pos), y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());

            FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
            FontRenderers.getCustomNormal(14).drawString(matrixStack, setting.getName(), x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
            FontRenderers.getCustomNormal(14).drawString(matrixStack, setting.get().toString(), x + FontRenderers.getCustomNormal(14).getStringWidth("> ") + FontRenderers.getCustomNormal(14).getStringWidth(setting.getName() + " "), y + (height / 4), new Color(240, 0.1f, 0.70f).getRgb());
        }
        if (setting.getType() == Setting.Type.Double){
            this.height = height;

            double start =  ((Setting<Double>) setting).get() -  ((Setting<Double>) setting).getMin();
            double perc = start / (((Setting<Double>) setting).getMax() -  ((Setting<Double>) setting).getMin());
            double pos = perc * width;

            if (Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && drag){
                float position = mouseX - x;
                float percent = MathHelper.clamp(position / width, 0, 1);
                double value = ((Setting<Double>) setting).getMin() + (percent * (((Setting<Double>) setting).getMax() - ((Setting<Double>) setting).getMin()));
                ((Setting<Double>) setting).setValue(value);
            }

            DrawableHelper.fill(matrixStack, x, y, (int) (x + pos), y + height, clickGUI.abyssHighlightColor.get().getRgb());
            DrawableHelper.fill(matrixStack, (int) (x + pos), y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());

            FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
            FontRenderers.getCustomNormal(14).drawString(matrixStack, setting.getName(), x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
            FontRenderers.getCustomNormal(14).drawString(matrixStack, String.format("%." + setting.getInc() + "f", setting.get()).replace(",", "."), x + FontRenderers.getCustomNormal(14).getStringWidth("> ") + FontRenderers.getCustomNormal(14).getStringWidth(setting.getName() + " "), y + (height / 4), new Color(240, 0.1f, 0.70f).getRgb());
        }
        if (setting.getType() == Setting.Type.Color){
            this.height = height;
            DrawableHelper.fill(matrixStack, x, y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());

            if (!expanded) FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
            else FontRenderers.getCustomNormal(14).drawString(matrixStack, "v", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
            FontRenderers.getCustomNormal(14).drawString(matrixStack, setting.getName(), x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());


            if (Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && (rClick || lClick)){
                expanded = !expanded;
            }

            if (expanded){
                this.height = height * 5;
                renderHue(matrixStack, x, y + height, width, height, drag);
                renderSaturation(matrixStack, x, y + (height * 2), width, height, drag);
                renderLightness(matrixStack, x, y + (height * 3), width, height, drag);
                renderAlpha(matrixStack, x, y + (height * 4), width, height, drag);
            }
        }
        if (setting.getType() == Setting.Type.Enum){
            this.height = height;

            if(Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y + 1, width, height - 1)) && lClick){
                var objects = ((Setting<Enum<?>>) setting).gets();
                int value = 0;
                for(int i =0; i < objects.size(); i++){
                    if(objects.get(i).equals(((Setting<Enum<?>>) setting).get())) value = i;
                }
                if(value != objects.size() - 1){
                    ((Setting<Enum<?>>) setting).setValue(((Setting<Enum<?>>) setting).gets().get(value + 1));
                }else{
                    ((Setting<Enum<?>>) setting).setValue(((Setting<Enum<?>>) setting).gets().get(0));
                }
            }

            DrawableHelper.fill(matrixStack, x, y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());

            FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
            FontRenderers.getCustomNormal(14).drawString(matrixStack, setting.getName(), x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
            FontRenderers.getCustomNormal(14).drawString(matrixStack, ((Setting<Enum<?>>) setting).get().name(), x + FontRenderers.getCustomNormal(14).getStringWidth("> ") + FontRenderers.getCustomNormal(14).getStringWidth(setting.getName() + " "), y + (height / 4), new Color(240, 0.1f, 0.70f).getRgb());
        }
        if (setting.getType() == Setting.Type.Keybind){
            this.height = height;

            if(Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && (lClick || rClick)){
                this.listening = true;

                try {
                    CommandManager.dispatch("bindsetting set " + setting.getName());
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            int keyBind = ((Setting<Keybind>) setting).get().get();

            DrawableHelper.fill(matrixStack, x, y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());

            FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
            FontRenderers.getCustomNormal(14).drawString(matrixStack, setting.getName(), x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
            FontRenderers.getCustomNormal(14).drawString(matrixStack, listening ? "..." : (keyBind == -1 ? "NONE" : KeyBinds.getKeyName(keyBind)), x + FontRenderers.getCustomNormal(14).getStringWidth("> ") + FontRenderers.getCustomNormal(14).getStringWidth("Key: "), y + (height / 4), new Color(240, 0.1f, 0.70f).getRgb());

        }
    }

    @SuppressWarnings("unchecked")
    public void renderAlpha(MatrixStack matrixStack, int x, int y, int width, int height, boolean drag){
        float start = ((Setting<Color>)setting).get().getAlpha();
        float perc = start / 255;
        float pos = perc * width;

        if (Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && drag){
            float position = mouseX - x + (50 / 255);
            float percent = MathHelper.clamp(position / width, 0, 1);
            int value = ((int) (percent * 255));
            ((Setting<Color>)setting).get().setAlpha(Math.round(value));
        }

        DrawableHelper.fill(matrixStack, x, y, (int) (x + pos), y + height, clickGUI.abyssHighlightColor.get().getRgb());
        DrawableHelper.fill(matrixStack, (int) (x + pos), y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());

        FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, "Alpha", x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, String.valueOf(((Setting<Color>) setting).get().getAlpha()), x + FontRenderers.getCustomNormal(14).getStringWidth("> ") + FontRenderers.getCustomNormal(14).getStringWidth(setting.getName() + " "), y + (height / 4), new Color(240, 0.1f, 0.70f).getRgb());

    }

    @SuppressWarnings("unchecked")
    public void renderSaturation(MatrixStack matrixStack, int x, int y, int width, int height, boolean drag){
        double start = ((Setting<Color>) setting).get().getSaturation();
        double perc = start;
        double pos = perc * width;

        if (Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && drag){
            float position = mouseX - x;
            float percent = MathHelper.clamp(position / width, 0, 1);
            double value = percent;
            ((Setting<Color>) setting).get().setSaturation((float) value);
        }

        DrawableHelper.fill(matrixStack, x, y, (int) (x + pos), y + height, clickGUI.abyssHighlightColor.get().getRgb());
        DrawableHelper.fill(matrixStack, (int) (x + pos), y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());

        FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, "Saturation", x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, String.format("%.3f", ((Setting<Color>) setting).get().getSaturation()), x + FontRenderers.getCustomNormal(14).getStringWidth("> ") + FontRenderers.getCustomNormal(14).getStringWidth("Saturation "), y + (height / 4), new Color(240, 0.1f, 0.70f).getRgb());
    }

    @SuppressWarnings("unchecked")
    public void renderLightness(MatrixStack matrixStack, int x, int y, int width, int height, boolean drag){
        double start = ((Setting<Color>) setting).get().getLightness();
        double perc = start;
        double pos = perc * width;

        if (Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && drag){
            float position = mouseX - x;
            float percent = MathHelper.clamp(position / width, 0, 1);
            double value = percent;
            ((Setting<Color>) setting).get().setLightness((float) value);
        }

        DrawableHelper.fill(matrixStack, x, y, (int) (x + pos), y + height, clickGUI.abyssHighlightColor.get().getRgb());
        DrawableHelper.fill(matrixStack, (int) (x + pos), y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());

        FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, "Lightness", x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, String.format("%.3f", ((Setting<Color>) setting).get().getLightness()), x + FontRenderers.getCustomNormal(14).getStringWidth("> ") + FontRenderers.getCustomNormal(14).getStringWidth("Lightness "), y + (height / 4), new Color(240, 0.1f, 0.70f).getRgb());
    }

    @SuppressWarnings("unchecked")
    public void renderHue(MatrixStack matrixStack, int x, int y, int width, int height, boolean drag){
        double start = ((Setting<Color>) setting).get().getHue();
        double perc = start;
        double pos = perc * width;

        if (Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && drag){
            float position = mouseX - x;
            float percent = MathHelper.clamp(position / width, 0, 1);
            double value = percent;
            ((Setting<Color>) setting).get().setHue((float) value);
        }

        DrawableHelper.fill(matrixStack, x, y, (int) (x + pos), y + height, clickGUI.abyssHighlightColor.get().getRgb());
        DrawableHelper.fill(matrixStack, (int) (x + pos), y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());

        FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, "Hue", x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, String.format("%.3f", ((Setting<Color>) setting).get().getHue()), x + FontRenderers.getCustomNormal(14).getStringWidth("> ") + FontRenderers.getCustomNormal(14).getStringWidth("Hue "), y + (height / 4), new Color(240, 0.1f, 0.70f).getRgb());
    }
}
