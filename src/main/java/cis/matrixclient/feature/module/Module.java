package cis.matrixclient.feature.module;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.manager.SettingManager;
import cis.matrixclient.feature.module.modules.client.ClickGUI;
import cis.matrixclient.feature.module.modules.movement.AutoWalk;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Keybind;
import cis.matrixclient.util.player.ChatUtils;
import cis.matrixclient.util.render.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Module {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    public String name;
    public Category category;
    public final Keybind bind = Keybind.none();
    public boolean enabled, drawn;
    public double arrayAnimation = -1;

    private final Page defaultPage = new Page("Default");
    public List<Page> groups = new ArrayList<>(1);

    public Module(){
        Info info = getClass().getAnnotation(Info.class);
        this.name = info.name();
        this.category = info.category();
        this.enabled = false;
        this.drawn = info.drawn();
    }

    public void onEnable(){}
    public void onDisable(){}

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public boolean getDrawn() { return drawn; }

    public void setName(String value) { name = value; }
    public void setEnabled(boolean value) { enabled = value; }
    public void setDrawn(boolean value) { drawn = value; }

    public void toggle(){
        if (enabled){
            setEnabled(false);
            sendToggledMsg();
            MatrixClient.EVENT_BUS.unregister(this);
            onDisable();
        }
        else {
            setEnabled(true);
            sendToggledMsg();
            MatrixClient.EVENT_BUS.register(this);
            onEnable();
        }
    }

    public void sendToggledMsg() {
        if (this == ModuleManager.getModule(ClickGUI.class)) return;
        info(Formatting.GRAY + "Toggled " + (isEnabled() ? Formatting.GREEN + "on" : Formatting.RED + "off"));
    }

    public String getInfo(){
        return "";
    }

    protected Setting<Integer> register(final String name, final int value, final int min, final int max){
        final Setting<Integer> s = new Setting<>(name, this, value, min, max, defaultPage);
        if (!groups.contains(defaultPage)) groups.add(defaultPage);
        SettingManager.addSetting(s);
        return s;
    }

    protected Setting<Integer> register(final String name, final int value, final int min, final int max, Page page){
        final Setting<Integer> s = new Setting<>(name, this, value, min, max, page);
        if (!groups.contains(page)) groups.add(page);
        SettingManager.addSetting(s);
        return s;
    }

    protected Setting<Double> register(final String name, final double value, final double min, final double max, int inc){
        final Setting<Double> s = new Setting<>(name, this, value, min, max, inc, defaultPage);
        if (!groups.contains(defaultPage)) groups.add(defaultPage);
        SettingManager.addSetting(s);
        return s;
    }

    protected Setting<Double> register(final String name, final double value, final double min, final double max, int inc, Page page){
        final Setting<Double> s = new Setting<>(name, this, value, min, max, inc, page);
        if (!groups.contains(page)) groups.add(page);
        SettingManager.addSetting(s);
        return s;
    }

    protected Setting<Boolean> register(final String name, final boolean value) {
        final Setting<Boolean> s = new Setting<>(name, this, value, Setting.Type.Boolean, defaultPage);
        if (!groups.contains(defaultPage)) groups.add(defaultPage);
        SettingManager.addSetting(s);
        return s;
    }

    protected Setting<Boolean> register(final String name, final boolean value, Page page) {
        final Setting<Boolean> s = new Setting<>(name, this, value, Setting.Type.Boolean, page);
        if (!groups.contains(page)) groups.add(page);
        SettingManager.addSetting(s);
        return s;
    }

    protected Setting<Color> register(final String name, final Color value) {
        final Setting<Color> s = new Setting<>(name, this, value, Setting.Type.Color, defaultPage);
        if (!groups.contains(defaultPage)) groups.add(defaultPage);
        SettingManager.addSetting(s);
        return s;
    }

    protected Setting<Color> register(final String name, final Color value, Page page) {
        final Setting<Color> s = new Setting<>(name, this, value, Setting.Type.Color, page);
        if (!groups.contains(page)) groups.add(page);
        SettingManager.addSetting(s);
        return s;
    }

    protected <T extends Enum<T>> Setting<T> register(final String name, T tEnum, T[] tEnums){
        final Setting<T> s = new Setting<>(name, this, tEnum, Arrays.stream(tEnums).toList(), defaultPage);
        if (!groups.contains(defaultPage)) groups.add(defaultPage);
        SettingManager.addSetting(s);
        return s;
    }

    protected <T extends Enum<T>> Setting<T> register(final String name, T tEnum, T[] tEnums, Page page){
        final Setting<T> s = new Setting<>(name, this, tEnum, Arrays.stream(tEnums).toList(), page);
        if (!groups.contains(page)) groups.add(page);
        SettingManager.addSetting(s);
        return s;
    }

    protected Setting<Keybind> register(final String name, final Keybind value) {
        final Setting<Keybind> s = new Setting<>(name, this, value, Setting.Type.Keybind, defaultPage);
        if (!groups.contains(defaultPage)) groups.add(defaultPage);
        SettingManager.addSetting(s);
        return s;
    }

    protected Setting<Keybind> register(final String name, final Keybind value, Page page) {
        final Setting<Keybind> s = new Setting<>(name, this, value, Setting.Type.Keybind, page);
        if (!groups.contains(page)) groups.add(page);
        SettingManager.addSetting(s);
        return s;
    }

    public void info(String message) {
        ChatUtils.info(Formatting.GRAY + message, this);
    }

    public void warning(String message) {
        ChatUtils.info(Formatting.YELLOW + message, this);
    }
    public void error(String message) {
        ChatUtils.info(Formatting.RED + message, this);
    }

    public enum Category{
        COMBAT,
        MOVEMENT,
        PLAYER,
        RENDER,
        MISC,
        WORLD,
        CLIENT
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Info {
        String name();

        Category category();

        boolean drawn() default true;
    }
}
