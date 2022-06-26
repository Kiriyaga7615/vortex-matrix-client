package cis.matrixclient.feature.module.modules.render;

import cis.matrixclient.event.events.render.Render3DEvent;
import cis.matrixclient.feature.manager.HoleManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.render.Renderer3D;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@Module.Info(name = "HoleESP", category = Module.Category.RENDER, drawn = true)
public class HoleESP extends Module {
    public Setting<Double> radiusX = register("RadiusX", 7.0, 1.0, 13, 2);
    public Setting<Double> radiusY = register("RadiusY", 3, 1.0, 8, 2);
    public Setting<Boolean> ignoreObby = register("IgnoreObby", false);
    public Setting<Boolean> ignoreOwn = register("IgnoreOwn", true);

    public Setting<Color> safeColor = register("SafeColor", new Color(0.93f, 0.54f, 0.50f));
    public Setting<Color> obbyColor = register("ObbyColor", new Color(0.32f, 0.70f, 0.57f));
    public Setting<Double> yBox = register("YBox", 1, 0.1, 1, 2);


    @Subscribe
    private void onRender(Render3DEvent event){
        List<HoleManager.Hole> holes = HoleManager.getHoles(radiusX.get(), radiusY.get());
        if (holes.isEmpty()) return;

        for (HoleManager.Hole hole : holes){
            if (ignoreOwn.get() && (hole.pos.equals(mc.player.getBlockPos()) | hole.pos2.equals(mc.player.getBlockPos()))) continue;
            if (ignoreObby.get() && hole.type == HoleManager.Hole.Type.Obsidian) continue;
            if (hole.pos2.equals(hole.pos.down())) continue;
            int color = hole.type == HoleManager.Hole.Type.Bedrock ? safeColor.get().getRgb() : obbyColor.get().getRgb();

            Direction offset = null;

            Vec3d vec3d = new Vec3d(hole.pos.getX(), hole.pos.getY(), hole.pos.getZ());

            Vec3d fixedVec = Renderer3D.getRenderPosition(vec3d);

            Box box = new Box(fixedVec.x, fixedVec.y, fixedVec.z, fixedVec.x + 1, fixedVec.y + 1, fixedVec.z + 1);

            if (hole.isDouble) {
                if (hole.pos.east().equals(hole.pos2)) offset = Direction.WEST;
                if (hole.pos.west().equals(hole.pos2)) offset = Direction.EAST;
                if (hole.pos.south().equals(hole.pos2)) offset = Direction.NORTH;
                if (hole.pos.north().equals(hole.pos2)) offset = Direction.SOUTH;

                int x = hole.pos.getX();
                int y = hole.pos.getY();
                int z = hole.pos.getZ();

                switch (offset){
                    case EAST -> box = new Box(x - 1, y, z, x + 1, y + yBox.get(), z + 1);
                    case WEST -> box = new Box(x, y, z, x + 2, y + yBox.get(), z + 1);
                    case NORTH -> box = new Box(x, y, z, x + 1, y + yBox.get(), z + 2);
                    case SOUTH -> box = new Box(x, y, z - 1, x + 1, y + yBox.get(), z + 1);
                }
            }

            Renderer3D.get.drawBox(event.getMatrixStack(), box, color);
        }
    }
}
