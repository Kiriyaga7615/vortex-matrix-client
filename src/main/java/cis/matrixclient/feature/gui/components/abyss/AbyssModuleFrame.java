package cis.matrixclient.feature.gui.components.abyss;

import cis.matrixclient.feature.command.CommandManager;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.client.ClickGUI;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Input;
import cis.matrixclient.util.key.KeyBinds;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.render.font.FontRenderers;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AbyssModuleFrame {
    public Module module;
    public List<AbyssSettingFrame> abyssSettingFrames = new ArrayList<>();

    private List<Page> groups;
    private Pair<Page, Integer> activeGroup;

    private int x, y, width, height;
    private boolean expanded, listening;

    private final ClickGUI clickGUI = ModuleManager.getModule(ClickGUI.class);


    public AbyssModuleFrame(Module module) {
        this.module = module;
        groups = module.groups;
        if (!groups.isEmpty()) activeGroup = new Pair<>(groups.get(0), 0);
    }

    public void init() {
        if (groups.isEmpty()) return;
        abyssSettingFrames.clear();

        for (Page page : groups) {
            for (Setting<?> setting : page) {
                abyssSettingFrames.add(new AbyssSettingFrame(page, setting));
            }
        }
    }

    public void render(MatrixStack matrixStack, int x, int y, int width, int height, int mouseX, int mouseY, boolean lClick, boolean rClick, boolean drag) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if(Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height))) {
            if(lClick){
                ModuleManager.getModule(module.getClass()).toggle();
            } else if (rClick) {
                expanded = !expanded;
            }
        }

        boolean enable = ModuleManager.getModule(module.getClass()).isEnabled();

        DrawableHelper.fill(matrixStack,  x, y, x + width, y + height, enable ? clickGUI.abyssHighlightColor.get().getRgb() : new Color(0, 0, 0.04f).getRgb());

        FontRenderers.getCustomNormal(14).drawString(matrixStack, module.getName(), x, y + (height / 4), new Color(0, 0, 1f).getRgb());

        if (!expanded) FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x + width - 10, y + (height / 4), new Color(0, 0, 1f).getRgb());
        else FontRenderers.getCustomNormal(14).drawString(matrixStack, "v", x + width - 10, y + (height / 4), new Color(0, 0, 1f).getRgb());

        if (expanded) {
            int yi = y + height + (groups.size() > 1 ? height : 0);
            if (!abyssSettingFrames.isEmpty()){
                if (groups.size() > 1){
                    renderCurrentGroup(matrixStack, x, y + height, width, height, mouseX, mouseY, lClick);
                }
                for (AbyssSettingFrame abyssSettingFrame : getComponents(activeGroup.getLeft())){
                    abyssSettingFrame.render(matrixStack, x, yi, width, height, mouseX, mouseY, lClick, rClick, drag);
                    yi = yi + abyssSettingFrame.getHeight();
                }
            }
            renderDrawn(matrixStack, x, yi, width, height, mouseX, mouseY, lClick);
            renderBind(matrixStack, x, yi + height, mouseX, mouseY, lClick, rClick);
        }
    }

    public void renderDrawn(MatrixStack matrixStack, int x, int y, int width, int height, int mouseX, int mouseY, boolean lClick) {
        if (Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && lClick) {
            module.setDrawn(!module.drawn);
        }

        DrawableHelper.fill(matrixStack, x, y, x + width, y + height, module.drawn ? clickGUI.abyssHighlightColor.get().getRgb() : new Color(0, 0, 0.04f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, "Drawn", x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
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

        DrawableHelper.fill(matrixStack, x, y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());

        FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, "Key:", x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, listening ? "..." : (keyBind == -1 ? "NONE" : KeyBinds.getKeyName(keyBind)), x + FontRenderers.getCustomNormal(14).getStringWidth("> ") + FontRenderers.getCustomNormal(14).getStringWidth("Key: "), y + (height / 4), new Color(240, 0.1f, 0.70f).getRgb());
    }

    public void renderCurrentGroup(MatrixStack matrixStack, int x, int y, int width, int height, int mouseX, int mouseY, boolean leftClicked){
        if(Input.mouseIsOver(mouseX, mouseY, new Rectangle(x, y, width, height)) && leftClicked){
            if (groups.size() > 1){
                activeGroup = new Pair<>(activeGroup.getRight() + 1 == groups.size() ? groups.get(0) : groups.get(activeGroup.getRight() + 1), activeGroup.getRight() + 1 == groups.size() ? 0 : activeGroup.getRight() + 1);
            }
        }

        DrawableHelper.fill(matrixStack, x, y, x + width, y + height, new Color(0, 0, 0.04f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, ">", x, y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, "Page", x + FontRenderers.getCustomNormal(14).getStringWidth("> "), y + (height / 4), new Color(0, 0, 1f).getRgb());
        FontRenderers.getCustomNormal(14).drawString(matrixStack, activeGroup.getLeft().name, x + FontRenderers.getCustomNormal(14).getStringWidth("> ") + FontRenderers.getCustomNormal(14).getStringWidth("Page "), y + (height / 4), new Color(240, 0.1f, 0.70f).getRgb());
    }

    public List<AbyssSettingFrame> getComponents(Page page){
        return abyssSettingFrames.stream().filter(settingComponent -> settingComponent.page == page).collect(Collectors.toList());
    }

    public int getHeight(){
        if (expanded) {
            return height + (groups.size() > 1 ? height : 0) + (activeGroup == null ? 0 : getSettingHeight()) + height + height;
        }
        return height;
    }

    public int getSettingHeight(){
        int height = 0;

        for (AbyssSettingFrame settingComponent : getComponents(activeGroup.getLeft())){
            height += settingComponent.getHeight();
        }
        return height;
    }
}