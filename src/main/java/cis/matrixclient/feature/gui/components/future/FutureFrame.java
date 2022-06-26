package cis.matrixclient.feature.gui.components.future;

import cis.matrixclient.feature.gui.clickgui.FutureClickGUI;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.client.ClickGUI;
import cis.matrixclient.util.key.Input;
import cis.matrixclient.util.render.Color;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static cis.matrixclient.MatrixClient.mc;

public class FutureFrame {
    private final Module.Category category;
    private int x, y, width, height;

    private boolean drag, expanded = true;
    private final List<FutureModuleFrame> futureModuleFrames = new ArrayList<>();

    private final ClickGUI clickGUI = ModuleManager.getModule(ClickGUI.class);

    public FutureFrame(Module.Category category, int x, int y, int width, int height){
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void init(){
        List<Module> modules = ModuleManager.getModules(category);

        for (Module module : modules){
            futureModuleFrames.add(new FutureModuleFrame(module));
        }

        for (FutureModuleFrame moduleButton : futureModuleFrames){
            moduleButton.init();
        }

        futureModuleFrames.sort(Comparator.comparing(moduleButton -> moduleButton.module.getName()));
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, boolean lClick, boolean rClick, boolean drag){
        Rectangle rect = new Rectangle(x, y, width, height);

        if(drag && Input.mouseIsOver(mouseX, mouseY, rect)) this.drag = true;
        else if(!drag) this.drag = false;

        if(rClick && Input.mouseIsOver(mouseX, mouseY, rect)) expanded = !expanded;

        if (this.drag && (FutureClickGUI.activeFutureFrame == this && mc.currentScreen instanceof FutureClickGUI)){
            setX(mouseX - 50);
            setY(mouseY - 7);
        }

        DrawableHelper.fill(matrixStack, x, y, x + width, y + height, clickGUI.futureHighlightColor.get().getRgb());
        mc.textRenderer.drawWithShadow(matrixStack, getCategoryName(), x + 3, y + 4, -1);
        DrawableHelper.fill(matrixStack, x, y + height, x + width, y + height + 1, new Color(0, 0, 0.11f, 200).getRgb());

        if (expanded){
            int yi = y + height + 1;
            for (FutureModuleFrame futureModuleFrame : futureModuleFrames){
                futureModuleFrame.render(matrixStack, x, yi, width, height, mouseX, mouseY, lClick, rClick, drag);
                yi += futureModuleFrame.getHeight();
            }
            DrawableHelper.fill(matrixStack, x, yi, x + width, yi + 2, new Color(0, 0, 0.11f, 200).getRgb());
        }
    }

    public String getCategoryName(){
        return category.name().substring(0,1).toUpperCase() + category.name().substring(1).toLowerCase();
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
