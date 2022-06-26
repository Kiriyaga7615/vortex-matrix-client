package cis.matrixclient.feature.gui.components.abyss;

import cis.matrixclient.feature.gui.clickgui.AbyssClickGUI;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.client.ClickGUI;
import cis.matrixclient.util.key.Input;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.render.font.FontRenderers;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static cis.matrixclient.MatrixClient.mc;

public class AbyssFrame {
    private final Module.Category category;
    private int x, y, width, height;

    private boolean drag, expanded = true;
    private final List<AbyssModuleFrame> abyssModuleFrames = new ArrayList<>();

    private final ClickGUI clickGUI = ModuleManager.getModule(ClickGUI.class);

    public AbyssFrame(Module.Category category, int x, int y, int width, int height){
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void init(){
        List<Module> modules = ModuleManager.getModules(category);

        for (Module module : modules){
            abyssModuleFrames.add(new AbyssModuleFrame(module));
        }

        for (AbyssModuleFrame moduleButton : abyssModuleFrames){
            moduleButton.init();
        }

        abyssModuleFrames.sort(Comparator.comparing(moduleButton -> moduleButton.module.getName()));
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, boolean lClick, boolean rClick, boolean drag){
        Rectangle rect = new Rectangle(x, y, width, height);

        if(drag && Input.mouseIsOver(mouseX, mouseY, rect)) this.drag = true;
        else if(!drag) this.drag = false;

        if(rClick && Input.mouseIsOver(mouseX, mouseY, rect)) expanded = !expanded;

        if (this.drag && (AbyssClickGUI.activeAbyssFrame == this && mc.currentScreen instanceof AbyssClickGUI)){
            setX(mouseX - 50);
            setY(mouseY - 7);
        }

        DrawableHelper.fill(matrixStack, x, y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());
        FontRenderers.getCustomNormal(16).drawCenteredString(matrixStack, category.name(), x + (width / 2), y + (height / 4), new Color(0, 0, 1f).getRgb());

        if (expanded){
            int yi = y + height + 2;
            for (AbyssModuleFrame abyssModuleFrame : abyssModuleFrames){
                DrawableHelper.fill(matrixStack, x, yi - 2, x + width,  yi, clickGUI.abyssHighlightColor.get().getRgb());
                abyssModuleFrame.render(matrixStack, x, yi, width, height, mouseX, mouseY, lClick, rClick, drag);
                yi += abyssModuleFrame.getHeight() + 2;
            }
        }
    }

    public int getX(){ return x; }
    public int getY(){ return y; }
    public int getWidth(){ return width; }
    public int getHeight(){ return height; }
    public boolean getExpanded() { return expanded; }
    public boolean getDrag() { return drag; }
    public Module.Category getCategory() { return category; }

    public void setX(int value) { x = value; }
    public void setY(int value) { y = value; }
    public void setExpanded(boolean bool) { expanded = bool; }
    public void setDrag(boolean bool) {drag = bool; }
}
