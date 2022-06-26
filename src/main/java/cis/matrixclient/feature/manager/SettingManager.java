package cis.matrixclient.feature.manager;

import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Keybind;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SettingManager {
    private static List<Setting<?>> settings;

    public static void init() {
        settings = new ArrayList<>();
    }

    public static List<Setting<?>> getSettings() {
        return settings;
    }

    public static List<Setting<?>> getSettings(Module module) {
        return settings.stream().filter(setting -> setting.getModule() == module).collect(Collectors.toList());
    }

    public static List<Setting<?>> getSettings(Setting.Type type){
        return settings.stream().filter(setting -> setting.getType() == type).collect(Collectors.toList());
    }

    public static Setting<?> getSetting(List<Setting<?>> settings, String name){
        for (Setting<?> setting : settings){
            if (setting.getName().equalsIgnoreCase(name)) return setting;
        }
        return null;
    }

    public static void addSetting(final Setting setting) {
        settings.add(setting);
    }
}
