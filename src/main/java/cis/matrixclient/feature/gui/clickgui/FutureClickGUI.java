package cis.matrixclient.feature.gui.clickgui;

import cis.matrixclient.feature.gui.GuiScreen;
import cis.matrixclient.feature.gui.components.future.FutureFrame;
import cis.matrixclient.feature.manager.ConfigManager;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.client.ClickGUI;
import cis.matrixclient.util.key.Input;
import cis.matrixclient.util.render.Color;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cis.matrixclient.MatrixClient.mc;

public class FutureClickGUI extends GuiScreen {
    public static List<FutureFrame> futureFrames = new ArrayList<>();
    public static FutureFrame activeFutureFrame;

    private boolean click, rClick, lClick;
    private boolean drag;
    private static double scroll;

    @Override
    protected void init() {
        futureFrames.clear();
        int x = 2, y = 2;

        for (Module.Category category : Module.Category.values()){

            futureFrames.add(new FutureFrame(category, x, y, 95, 15));
            if (x + 220 >= mc.getWindow().getScaledWidth()){
                x = 2;
                y += 20;
            }else x += 120;
        }

        try {
            ConfigManager.loadGUI();
        } catch (IOException ignored) {}
        futureFrames.forEach(FutureFrame::init);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        DrawableHelper.fill(matrices, 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), new Color(0, 0, 0.05f, 110).getRgb());

        futureFrames.forEach(futureFrame -> futureFrame.render(matrices, mouseX, mouseY, lClick, rClick, drag));

        scroll = 0;
        if(click) {
            lClick = false;
            rClick = false;
        }
    }


    @Override
    public void close() {
        try {
            ConfigManager.saveGUI();
        } catch (IOException ignored) {}
        ModuleManager.getModule(ClickGUI.class).setEnabled(false);
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!click) {
            if(button == 0) {
                futureFrames.forEach(futureFrame -> {
                    if (Input.mouseIsOver(mouseX, mouseY, new Rectangle(futureFrame.getX(), futureFrame.getY(), futureFrame.getWidth(), futureFrame.getHeight()))){
                        activeFutureFrame = futureFrame;
                    }
                });
                drag = true; lClick = true; rClick = false; click = true;
            } else if(button == 1){
                lClick = false; rClick = true; click = true;
            }
        } else {
            lClick = false; rClick = false;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            activeFutureFrame = null;
            drag = false; lClick = false; rClick = false; click = false;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 256){
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (amount > 0){
            for (FutureFrame futureFrame : getFrames()){
                futureFrame.setY(futureFrame.getY() - 5);
            }
        }
        if (amount < 0){
            for (FutureFrame futureFrame : getFrames()){
                futureFrame.setY(futureFrame.getY() + 5);
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    public static List<FutureFrame> getFrames(){ return futureFrames; }
}
