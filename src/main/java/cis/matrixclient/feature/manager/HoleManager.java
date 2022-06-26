package cis.matrixclient.feature.manager;

import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.world.MsEvent;
import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.render.HoleESP;
import cis.matrixclient.util.player.PlayerUtils;
import cis.matrixclient.util.world.BlockUtils;
import cis.matrixclient.util.world.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static cis.matrixclient.MatrixClient.mc;

public class HoleManager {
    private final ExecutorService thread = Executors.newFixedThreadPool(1);

    public static List<Module> modules = new ArrayList<>(){{
            add(ModuleManager.getModule(HoleESP.class));
    }};

    public static List<Hole> holes = new ArrayList<>();
    public static double radiusX;
    public static double radiusY;

    public static void init(){
        MatrixClient.EVENT_BUS.register(new HoleManager());
    }

    public static List<Hole> getHoles(double radiusX, double radiusY){
        HoleManager.radiusX = radiusX;
        HoleManager.radiusY = radiusY;
        return holes;
    }

    @Subscribe
    private void onTick(TickEvent.Post event){
        if (!WorldUtils.canUpdate()) return;
        if (!shouldWork()) return;

        thread.execute(this::calcHoles);
    }

    public void calcHoles(){
        if (!WorldUtils.canUpdate()) return;
        List<Hole> holes = new ArrayList<>();
        List<BlockPos> sphere = BlockUtils.getSphere(mc.player, radiusX, radiusY);

        int size = sphere.size();
        for (int i = 0; i < size; i++){
            BlockPos pos = sphere.get(i);
            if (pos == null) continue;
            if (BlockUtils.isSolid(pos) | BlockUtils.isSolid(pos.up()) | BlockUtils.isSolid(pos.up(2))) continue;
            if (!BlockUtils.isHole(pos) && !BlockUtils.isDoubleHole(pos)) continue;

            Hole hole = getHole(pos);
            if (hole == null) continue;
            boolean shouldSkip = false;

            for (Hole hole1 : holes){
                if (hole1.pos.equals(hole.pos) | hole1.pos.equals(hole.pos2)) shouldSkip = true;
            }

            if (BlockUtils.isSolid(hole.pos2.up())) shouldSkip = true;

            if (shouldSkip) continue;

            holes.add(hole);
        }

        HoleManager.holes = holes;
    }

    public static Hole getHole(BlockPos pos){
        BlockPos pos2 = pos;
        boolean isSafe = true;
        boolean isDouble = false;

        for (Direction offset1 : Direction.values()){
            if (offset1 == Direction.UP) continue;

            BlockPos offset1Pos = pos.offset(offset1);

            if (BlockUtils.isBlastRes(offset1Pos) && BlockUtils.getBlock(offset1Pos) != Blocks.BEDROCK) {
                isSafe = false;
            }

            if (!BlockUtils.isSolid(offset1Pos) && offset1 == Direction.DOWN){
                return null;
            }

            if (!BlockUtils.isSolid(offset1Pos) && offset1 != Direction.DOWN) {
                pos2 = offset1Pos;
                isDouble = true;

                for (Direction offset2 : Direction.values()){
                    if (offset2 == Direction.UP) continue;
                    BlockPos offset2Pos = pos.offset(offset2);

                    if (BlockUtils.isBlastRes(offset2Pos) && BlockUtils.getBlock(offset2Pos) != Blocks.BEDROCK) {
                        isSafe = false;
                    }
                }
            }
        }
        return new Hole(pos, pos2, isSafe ? Hole.Type.Bedrock : Hole.Type.Obsidian, isDouble);
    }

    public static boolean shouldWork(){
        for (Module module : modules){
            if (module.enabled) return true;
        }
        return false;
    }

    public static class Hole{
        public BlockPos pos;
        public BlockPos pos2;
        public Type type;
        public boolean isDouble;

        public Hole(BlockPos pos, BlockPos pos2, Type type,boolean isDouble){
            this.pos = pos;
            this.pos2 = pos2;
            this.type = type;
            this.isDouble = isDouble;
        }

        public enum Type{
            Bedrock,
            Obsidian
        }
    }
}
