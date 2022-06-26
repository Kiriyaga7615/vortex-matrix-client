package cis.matrixclient.util.player;

import cis.matrixclient.feature.module.Module;
import cis.matrixclient.util.world.WorldUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static cis.matrixclient.MatrixClient.mc;


public class ChatUtils {
    private static final String prefix = Formatting.GRAY + "[" + Formatting.LIGHT_PURPLE + "MatrixClient" + Formatting.GRAY + "] " + Formatting.RESET;

    private static void send(String str){
        if (!WorldUtils.canUpdate()) return;
        mc.inGameHud.getChatHud().addMessage(Text.of(str));
    }

    public static void info(String str){
        send(prefix + Formatting.GRAY + str);
    }
    public static void info(String str, Module module) {
        send(prefix + Formatting.GRAY + "(" + Formatting.WHITE + module.getName() + Formatting.GRAY + ") " + Formatting.GRAY + str);
    }

    public static void warn(String str){
        send(prefix + Formatting.YELLOW + str);
    }
    public static void warn(String str, Module module) {
        send(prefix + Formatting.GRAY + "(" + Formatting.WHITE + module.getName() + Formatting.GRAY + ") " +  Formatting.YELLOW + str);
    }

    public static void error(String str){
        send(prefix + Formatting.RED + str);
    }
    public static void error(String str, Module module) {
        send(prefix + Formatting.GRAY + "(" + Formatting.WHITE + module.getName() + Formatting.GRAY + ") " +  Formatting.RED + str);
    }
}
