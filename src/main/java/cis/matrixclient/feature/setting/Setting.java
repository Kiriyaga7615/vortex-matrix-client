package cis.matrixclient.feature.setting;

import cis.matrixclient.feature.module.Module;
import cis.matrixclient.util.render.Color;

import java.util.ArrayList;
import java.util.List;

public class Setting<T> {
    private final String name;
    private final Module module;
    private final Type type;

    private T value;
    private T min;
    private T max;
    private List<T> values;
    private int inc;

    private Page page;

    public Setting(final String name, final Module module, T value, Type type, Page page){
        this.name = name;
        this.module = module;
        this.type = type;
        this.value = value;
        this.page = page;
        this.page.settings.add(this);
    }


    public Setting(final String name, final Module module, T value, T min, T max, Page page){
        this.name = name;
        this.module = module;
        this.type = Type.Integer;
        this.value = value;
        this.min = min;
        this.max = max;
        this.page = page;
        this.page.settings.add(this);
    }

    public Setting(final String name, final Module module, T value, T min, T max, int inc, Page page){
        this.name = name;
        this.module = module;
        this.type = Type.Double;
        this.value = value;
        this.min = min;
        this.max = max;
        this.inc = inc;
        this.page = page;
        this.page.settings.add(this);
    }

    public Setting(final String name, final Module module, T value, List<T> values, Page page){
        this.name = name;
        this.module = module;
        this.type = Type.Enum;
        this.value = value;
        this.values = values;
        this.page = page;
        this.page.settings.add(this);
    }

    public String getName() { return name; }
    public Module getModule() { return module; }
    public Type getType() { return type; }

    public T get() { return value; }
    public boolean get(T value) { return this.value == value; }
    public T getMin() { return min; }
    public T getMax() { return max; }

    public int getInc() { return inc; }

    public List<T> gets(){ return values.stream().toList(); }

    public void setValue(T value) {
        this.value = value;
    }

    public enum Type {
        Integer, Double, Boolean, Enum, Color, Keybind
    }

}
