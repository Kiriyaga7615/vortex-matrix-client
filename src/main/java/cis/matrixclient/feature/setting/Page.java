package cis.matrixclient.feature.setting;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Page implements Iterable<Setting<?>>{
    public final String name;
    final List<Setting<?>> settings = new ArrayList<>(1);

    public Page(String name) {
        this.name = name;
    }

    public Setting<?> get(String name) {
        for (Setting<?> setting : this) {
            if (setting.getName().equals(name)) return setting;
        }

        return null;
    }

    public <T> Setting<T> add(Setting<T> setting) {
        settings.add(setting);

        return setting;
    }

    @NotNull
    @Override
    public Iterator<Setting<?>> iterator() {
        return settings.iterator();
    }
}
