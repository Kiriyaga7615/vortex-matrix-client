package cis.matrixclient.feature.gui.components.future;

import cis.matrixclient.feature.command.CommandManager;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.client.ClickGUI;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Input;
import cis.matrixclient.util.key.KeyBinds;
import cis.matrixclient.util.render.Color;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cis.matrixclient.MatrixClient.mc;

public class FutureModuleFrame {
    public Module module;
    public List<FutureSettingFrame> futureSettingFrames = new ArrayList<>();
    private int x, y, width, height;
    private boolean expanded, listening;

    private List<Page> groups;
    private Pair<Page, Integer> activeGroup;

    private final ClickGUI clickGUI = ModuleManager.getModule(ClickGUI.class);

    public FutureModuleFrame(Module module) {
        this.module = module;
        groups = module.groups;
        if (!groups.isEmpty()) activeGroup = new Pair<>(groups.get(0), 0);
    }

    public void init() {
        if (groups.isEmpty()) return;
        futureSettingFrames.clear();

        for (Page page : groups) {
            for (Setting<?> setting : page) {
                futureSettingFrames.add(new FutureSettingFrame(page, setting));
            }
        }
    }

    public void render(MatrixStack matrixStack, int x, int y, int width, int height, int mouseX, int mouseY, boolean lClick, boolean rClick, boolean drag) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if (Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height))) {
            if (lClick) {
                ModuleManager.getModule(module.getClass()).toggle();
            } else if (rClick) {
                expanded = !expanded;
            }
        }

        boolean enable = ModuleManager.getModule(module.getClass()).isEnabled();

        DrawableHelper.fill(matrixStack, x, y, x + 2, y + height, new Color(0, 0, 0.11f, 200).getRgb());
        DrawableHelper.fill(matrixStack, x + width - 2, y, x + width, y + height, new Color(0, 0, 0.11f, 200).getRgb());

        DrawableHelper.fill(matrixStack, x + 2, y, x + width - 2, y + 1, new Color(0, 0, 0.11f, 200).getRgb());

        DrawableHelper.fill(matrixStack, x + 2, y + 1, x + width - 2, y + height, enable ? clickGUI.futureHighlightColor.get().getRgb() : new Color(0, 0, 0.11f, 100).getRgb());

        renderGear(x, y, width);
        mc.textRenderer.drawWithShadow(matrixStack, module.getName(), x + 4, y + 4, enable ? -1 : new Color(0, 0, 0.7f).getRgb());

        if (expanded) {
            int yi = y + height + (groups.size() > 1 ? height : 0);
            if (!futureSettingFrames.isEmpty()){
                if (groups.size() > 1){
                    renderCurrentGroup(matrixStack, x, y + height, width, height, mouseX, mouseY, lClick);
                }
                for (FutureSettingFrame futureSettingFrame : getComponents(activeGroup.getLeft())){
                    futureSettingFrame.render(matrixStack, x, yi, width, height, mouseX, mouseY, lClick, rClick, drag);
                    yi = yi + futureSettingFrame.getHeight();
                }
            }

            renderDrawn(matrixStack, x, yi, width, height, mouseX, mouseY, lClick);
            renderBind(matrixStack, x, yi + height, mouseX, mouseY, lClick, rClick);
        }
    }

    public List<FutureSettingFrame> getComponents(Page page){
        return futureSettingFrames.stream().filter(settingComponent -> settingComponent.page == page).collect(Collectors.toList());
    }

    public void renderCurrentGroup(MatrixStack matrixStack, int x, int y, int width, int height, int mouseX, int mouseY, boolean leftClicked){
        if(Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && leftClicked){
            if (groups.size() > 1){
                activeGroup = new Pair<>(activeGroup.getRight() + 1 == groups.size() ? groups.get(0) : groups.get(activeGroup.getRight() + 1), activeGroup.getRight() + 1 == groups.size() ? 0 : activeGroup.getRight() + 1);
            }
        }
        DrawableHelper.fill(matrixStack, x, y, x + 2, y + height, new Color(0, 0, 0.11f, 200).getRgb());
        DrawableHelper.fill(matrixStack, x + width - 2, y, x + width, y + height, new Color(0, 0, 0.11f, 200).getRgb());

        DrawableHelper.fill(matrixStack, x + 2, y, x + width - 2, y + 1, new Color(0, 0, 0.11f, 200).getRgb());


        DrawableHelper.fill(matrixStack, x + 2, y + 1, x + width - 2, y + height, new Color(0, 0, 0.11f, 100).getRgb());

        mc.textRenderer.drawWithShadow(matrixStack, "Page", x + 6, y + 4, -1);
        mc.textRenderer.drawWithShadow(matrixStack, activeGroup.getLeft().name, x + mc.textRenderer.getWidth("Page") + 10, y + 4, new Color(0, 0, 0.5f).getRgb());
    }


    public void renderDrawn(MatrixStack matrixStack, int x, int y, int width, int height, int mouseX, int mouseY, boolean lClick) {
        if (Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && lClick) {
            module.setDrawn(!module.drawn);
        }

        DrawableHelper.fill(matrixStack, x, y, x + 2, y + height, new Color(0, 0, 0.11f, 200).getRgb());
        DrawableHelper.fill(matrixStack, x + width - 2, y, x + width, y + height, new Color(0, 0, 0.11f, 200).getRgb());

        DrawableHelper.fill(matrixStack, x + 2, y, x + width - 2, y + 1, new Color(0, 0, 0.11f, 200).getRgb());


        DrawableHelper.fill(matrixStack, x + 2, y + 1, x + width - 2, y + height, module.drawn ? clickGUI.futureHighlightColor.get().getRgb() : new Color(0, 0, 0.11f, 100).getRgb());
        mc.textRenderer.drawWithShadow(matrixStack, "Drawn", x + 6, y + 4, -1);
    }

    public void renderBind(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY, boolean lClick, boolean rClick){
        if(Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && (lClick || rClick)){
            this.listening = true;

            try {
                CommandManager.dispatch("bind set " + module.getName());
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        int keyBind = ModuleManager.getModule(module.getClass()).bind.get();

        DrawableHelper.fill(matrixStack, x, y, x + 2, y + height, new Color(0, 0, 0.11f, 200).getRgb());
        DrawableHelper.fill(matrixStack, x + width - 2, y, x + width, y + height, new Color(0, 0, 0.11f, 200).getRgb());

        DrawableHelper.fill(matrixStack, x + 2, y, x + width - 2, y + 1, new Color(0, 0, 0.11f, 200).getRgb());


        DrawableHelper.fill(matrixStack, x + 2, y + 1, x + width - 2, y + height, new Color(0, 0, 0.11f, 100).getRgb());

        mc.textRenderer.drawWithShadow(matrixStack, "Keybind", x + 6, y + 4, -1);
        mc.textRenderer.drawWithShadow(matrixStack, listening ? "..." : (keyBind == -1 ? "NONE" : KeyBinds.getKeyName(keyBind)), x + mc.textRenderer.getWidth("Keybind") + 10, y + 4, new Color(0, 0, 0.7f).getRgb());
    }

    public void renderGear(int x, int y, int w){
        MatrixStack stacks = new MatrixStack();
        stacks.scale(1.0f, 1.0f, 1.0f);
        Identifier gear = new Identifier("picture", "gear.png");
        RenderSystem.setShaderTexture(0, gear);
        DrawableHelper.drawTexture(stacks, x + w - 13, y + 3, 0, 0, 10, 10, 10, 10);
    }

    public int getHeight(){
        if (expanded) {
            return height + (groups.size() > 1 ? height : 0) + (activeGroup == null ? 0 : getSettingHeight()) + height + height;
        }
        return height;
    }

    public int getSettingHeight(){
        int height = 0;

        for (FutureSettingFrame settingComponent : getComponents(activeGroup.getLeft())){
            height += settingComponent.getHeight();
        }
        return height;
    }
}
