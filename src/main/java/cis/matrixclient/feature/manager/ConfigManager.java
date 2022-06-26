package cis.matrixclient.feature.manager;

import cis.matrixclient.feature.gui.clickgui.AbyssClickGUI;
import cis.matrixclient.feature.gui.clickgui.FutureClickGUI;
import cis.matrixclient.feature.gui.components.abyss.AbyssFrame;
import cis.matrixclient.feature.gui.components.future.FutureFrame;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.client.ClickGUI;
import cis.matrixclient.feature.module.modules.render.Freecam;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Keybind;
import cis.matrixclient.util.render.Color;
import com.google.gson.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager extends Thread{
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final File mainFolder = new File("MatrixClient");

    private static final String modulesFolder = mainFolder.getAbsolutePath() + "/modules";
    private static final String futureFolder = mainFolder.getAbsolutePath() + "/futuregui";
    private static final String abyssFolder = mainFolder.getAbsolutePath() + "/abyssgui";
    private static final String friends = "Friends.json";

    public static ConfigManager INSTANCE;

    public ConfigManager(){
        INSTANCE = this;
    }

    public static void load(){
        try {
            loadModules();
            loadGUI();
            loadFriends();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadFriends() throws IOException {
        Path path = Path.of(mainFolder.getAbsolutePath(), friends);
        if (!path.toFile().exists()) return;
        String rawJson = loadFile(path.toFile());
        JsonObject jsonObject = new JsonParser().parse(rawJson).getAsJsonObject();
        if (jsonObject.get("Friends") != null) {
            JsonArray friendObject = jsonObject.get("Friends").getAsJsonArray();
            friendObject.forEach(object -> FriendManager.getFriends().add(object.getAsString()));
        }
    }

    public static void loadGUI() throws IOException {
        switch (ModuleManager.getModule(ClickGUI.class).guiMode.get()){
            case Abyss -> {
                for (AbyssFrame abyssFrame : AbyssClickGUI.getFrames()){
                    loadGUI(abyssFrame);
                }
            }
            case Future -> {
                for (FutureFrame futureFrame : FutureClickGUI.getFrames()){
                    loadGUI(futureFrame);
                }
            }
        }
    }

    public static void loadGUI(AbyssFrame f) throws IOException {
        Path path = Path.of(abyssFolder, f.getCategory().name() + ".json");
        if (!path.toFile().exists()) return;
        String rawJson = loadFile(path.toFile());
        JsonObject jsonObject = new JsonParser().parse(rawJson).getAsJsonObject();

        if (jsonObject.get("x") != null && jsonObject.get("y") != null && jsonObject.get("expanded") != null && jsonObject.get("drag") != null){
            f.setX(jsonObject.get("x").getAsInt());
            f.setY(jsonObject.get("y").getAsInt());
            f.setExpanded(jsonObject.get("expanded").getAsBoolean());
            f.setDrag(jsonObject.get("drag").getAsBoolean());
        }
    }

    public static void loadGUI(FutureFrame f) throws IOException {
        Path path = Path.of(futureFolder, f.getCategory().name() + ".json");
        if (!path.toFile().exists()) return;
        String rawJson = loadFile(path.toFile());
        JsonObject jsonObject = new JsonParser().parse(rawJson).getAsJsonObject();

        if (jsonObject.get("x") != null && jsonObject.get("y") != null && jsonObject.get("expanded") != null && jsonObject.get("drag") != null){
            f.setX(jsonObject.get("x").getAsInt());
            f.setY(jsonObject.get("y").getAsInt());
            f.setExpanded(jsonObject.get("expanded").getAsBoolean());
            f.setDrag(jsonObject.get("drag").getAsBoolean());
        }
    }

    private static void loadModules() throws IOException {
        for (Module m : ModuleManager.getModules()) {
            loadModule(m);
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadModule(Module m) throws IOException {
        try {
            Path path = Path.of(modulesFolder, m.getName() + ".json");
            if (!path.toFile().exists()) return;
            String rawJson = loadFile(path.toFile());
            JsonObject jsonObject = new JsonParser().parse(rawJson).getAsJsonObject();
            if (jsonObject.get("enabled") != null && jsonObject.get("drawn") != null && jsonObject.get("bind") != null) {
                if (jsonObject.get("enabled").getAsBoolean() && m != ModuleManager.getModule(Freecam.class)) m.toggle();
                m.setDrawn(jsonObject.get("drawn").getAsBoolean());
                m.bind.set(true, jsonObject.get("bind").getAsInt());
            }

            SettingManager.getSettings(m).forEach(s -> {
                JsonElement settingObject = jsonObject.get(s.getName());
                if (settingObject != null) {
                    switch (s.getType()){
                        case Boolean -> ((Setting<Boolean>) s).setValue(settingObject.getAsBoolean());
                        case Double -> ((Setting<Double>) s).setValue(settingObject.getAsDouble());
                        case Enum -> ((Setting<Enum<?>>) s).setValue(Enum.valueOf(((Setting<Enum<?>>) s).get().getClass(), settingObject.getAsString()));
                        case Integer -> ((Setting<Integer>) s).setValue(settingObject.getAsInt());
                        case Color -> {
                            String[] strings = settingObject.getAsString().split(" ");
                            ((Setting<Color>) s).get().setHue(Float.parseFloat(strings[0]));
                            ((Setting<Color>) s).get().setSaturation(Float.parseFloat(strings[1]));
                            ((Setting<Color>) s).get().setLightness(Float.parseFloat(strings[2]));
                            if (strings.length == 4) ((Setting<Color>) s).get().setAlpha(Integer.parseInt(strings[3]));
                        }
                        case Keybind -> ((Setting<Keybind>) s).setValue(new Keybind(true, settingObject.getAsInt()));
                    }
                }
            });
        }catch (JsonSyntaxException ignored){}

    }

    public static String loadFile(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file.getAbsolutePath());
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }


    @Override
    public void run() {
        if (!mainFolder.exists() && !mainFolder.mkdirs()) System.out.println("Failed to create config folder");
        if (!new File(modulesFolder).exists() && !new File(modulesFolder).mkdirs())
            System.out.println("Failed to create modules folder");
        if (!new File(futureFolder).exists() && !new File(futureFolder).mkdirs())
            System.out.println("Failed to create gui folder");
        if (!new File(abyssFolder).exists() && !new File(abyssFolder).mkdirs())
            System.out.println("Failed to create gui folder");
        try {
            saveModules();
            saveGUI();
            saveFriends();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFriends() throws IOException {
        Path path = Path.of(mainFolder.getAbsolutePath(), friends);
        createFile(path);
        JsonObject jsonObject = new JsonObject();
        JsonArray friends = new JsonArray();
        FriendManager.getFriends().forEach(friends::add);
        jsonObject.add("Friends", friends);
        Files.write(path, gson.toJson(new JsonParser().parse(jsonObject.toString())).getBytes());
    }

    public static void saveGUI() throws IOException {
        switch (ModuleManager.getModule(ClickGUI.class).guiMode.get()){
            case Abyss -> {
                for (AbyssFrame f : AbyssClickGUI.getFrames()){
                    saveGUI(f);
                }
            }
            case Future -> {
                for (FutureFrame futureFrame : FutureClickGUI.getFrames()){
                    saveGUI(futureFrame);
                }
            }
        }
    }

    private static void saveGUI(AbyssFrame f) throws IOException {
        Path path = Path.of(abyssFolder, f.getCategory().name() + ".json");
        createFile(path);
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("x", f.getX());
        jsonObject.addProperty("y", f.getY());
        jsonObject.addProperty("expanded", f.getExpanded());
        jsonObject.addProperty("drag", f.getDrag());
        Files.write(path, gson.toJson(new JsonParser().parse(jsonObject.toString())).getBytes());
    }

    private static void saveGUI(FutureFrame f) throws IOException {
        Path path = Path.of(futureFolder, f.getCategory().name() + ".json");
        createFile(path);
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("x", f.getX());
        jsonObject.addProperty("y", f.getY());
        jsonObject.addProperty("expanded", f.getExpanded());
        jsonObject.addProperty("drag", f.getDrag());
        Files.write(path, gson.toJson(new JsonParser().parse(jsonObject.toString())).getBytes());
    }


    private void saveModules() throws IOException {
        for (Module m : ModuleManager.getModules()) {
            saveModule(m);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> void saveModule(Module m) throws IOException {
        Path path = Path.of(modulesFolder, m.getName() + ".json");
        createFile(path);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("enabled", m.enabled);
        jsonObject.addProperty("drawn", m.drawn);
        jsonObject.addProperty("bind", m.bind.get());
        SettingManager.getSettings(m).forEach(s -> {
            switch (s.getType()) {
                case Enum -> jsonObject.addProperty(s.getName(), ((Setting<Enum<T>>) s).get().name());
                case Boolean -> jsonObject.addProperty(s.getName(), (Boolean) s.get());
                case Double -> jsonObject.addProperty(s.getName(), (Double) s.get());
                case Integer -> jsonObject.addProperty(s.getName(), (Integer) s.get());
                case Color -> jsonObject.addProperty(s.getName(), ((Setting<Color>) s).get().hue + " " + ((Setting<Color>) s).get().saturation  + " " + ((Setting<Color>) s).get().lightness + " " + ((Setting<Color>) s).get().a);
                case Keybind -> jsonObject.addProperty(s.getName(), ((Setting<Keybind>) s).get().get());
            }
        });
        Files.write(path, gson.toJson(new JsonParser().parse(jsonObject.toString())).getBytes());
    }

    private static void createFile(Path path) {
        if (Files.exists(path)) new File(path.normalize().toString()).delete();
        try {
            Files.createFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
