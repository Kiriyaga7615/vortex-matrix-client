package cis.matrixclient.feature.manager;

import cis.matrixclient.feature.module.Module;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ModuleManager {
    private static ArrayList<Module> modules = new ArrayList<>();

    public static void init(){
        modules = new ArrayList<>();

        Set<Class<? extends Module>> reflections = new Reflections("cis.matrixclient.feature.module").getSubTypesOf(Module.class);
        reflections.forEach(aClass -> {
            try {
                if (aClass.isAnnotationPresent(Module.Info.class)) modules.add(aClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        modules.sort(Comparator.comparing(Module::getName));
    }

    public static ArrayList<Module> getModules() {
        return modules;
    }

    public static List<Module> getModules(Module.Category c) {
        List<Module> categoryModules = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == c)
                categoryModules.add(m);
        }
        return categoryModules;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T getModule(Class<T> klass){
        for (Module module : modules){
            if (module.getClass() == klass){
                return (T) module;
            }
        }
        return null;
    }

    public static Module getModule(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name))
                return m;
        }
        return null;
    }
}
