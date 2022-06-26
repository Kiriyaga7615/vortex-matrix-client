package cis.matrixclient.util.world;

import static cis.matrixclient.MatrixClient.mc;

public class WorldUtils {
    public static boolean canUpdate() {
        return mc != null && mc.world != null && mc.player != null;
    }
    public static boolean rendering3D = true;
    public static double frameTime;
}
