package cis.matrixclient.feature.module.modules.render;

import cis.matrixclient.event.events.render.Render3DEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.BlockPosX;
import cis.matrixclient.util.entity.EntityUtils;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.render.Renderer3D;
import cis.matrixclient.util.world.BlockUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.entity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.stream.Stream;

@Module.Info(name = "BlockESP", category = Module.Category.RENDER, drawn = true)
public class BlockESP extends Module {
    public Page storagePage = new Page("Storage");
    public Page storageRenderPage = new Page("StorageRender");

    public Page blockPage = new Page("Block");
    public Page blockRenderPage = new Page("BlockRender");

    public Setting<Integer> alpha = register("Alpha", 60, 1, 255);

    public Setting<Boolean> chest = register("Chest", false, storagePage);
    public Setting<Boolean> shulker = register("Shulker", true, storagePage);
    public Setting<Boolean> echest = register("Ender chest", false, storagePage);
    public Setting<Boolean> hopper = register("Hopper", false, storagePage);
    public Setting<Boolean> furnace = register("Furnace", false, storagePage);
    public Setting<Boolean> barrel = register("Barrel", false, storagePage);

    public Setting<Color> chestColor = register("ChestColor", new Color(0.137f, 0.411f, 0.960f), storageRenderPage);
    public Setting<Color> shulkerColor = register("ShulkerColor", new Color(0.747f, 0.747f, 0.960f), storageRenderPage);
    public Setting<Color> echestColor = register("EChestColor", new Color(0.800f, 0.830f, 0.960f), storageRenderPage);
    public Setting<Color> hopperColor = register("HopperColor", new Color(0.105f, 0.147f, 0.568f), storageRenderPage);
    public Setting<Color> furnaceColor = register("FurnaceColor", new Color(0.105f, 0.147f, 0.568f), storageRenderPage);
    public Setting<Color> barrelColor = register("BarrelColor", new Color(0.137f, 0.411f, 0.960f), storageRenderPage);

    public Setting<Boolean> bed = register("Bed", false, blockPage);
    public Setting<Boolean> netherPortal = register("NetherPortal", true, blockPage);
    public Setting<Boolean> endPortal = register("EndPortal", true, blockPage);

    public Setting<Color> bedColor = register("BedColor", new Color(0.980f, 0.860f, 0.860f), blockRenderPage);
    public Setting<Color> netherPortalColor = register("NetherPortalColor", new Color(0.684f, 0.789f, 0.863f), blockRenderPage);
    public Setting<Color> endPortalColor = register("EndPortalColor", new Color(0.600f, 0.460f, 0.863f), blockRenderPage);

    @Subscribe
    private void onRender(Render3DEvent event){
        blockEntities().forEach(blockEntity -> {
            BlockPos pos = blockEntity.getPos();
            Vec3d vec3d = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
            Vec3d fixedVec = Renderer3D.getRenderPosition(vec3d);

            Box box = new Box(fixedVec.x, fixedVec.y, fixedVec.z, fixedVec.x + 1, fixedVec.y + 1, fixedVec.z + 1);

            if (blockEntity instanceof ChestBlockEntity) box = new Box(fixedVec.x + 0.05, fixedVec.y, fixedVec.z + 0.05, fixedVec.x + 0.95, fixedVec.y + 0.90, fixedVec.z + 0.95);
            if (blockEntity instanceof BedBlockEntity) box = new Box(fixedVec.x, fixedVec.y, fixedVec.z, fixedVec.x + 1, fixedVec.y + 0.6, fixedVec.z + 1);
            if (blockEntity instanceof EndPortalBlockEntity) box = new Box(fixedVec.x, fixedVec.y + 0.70, fixedVec.z, fixedVec.x +1, fixedVec.y + 0.80, fixedVec.z + 1);

            Renderer3D.get.drawBox(event.getMatrixStack(), box, getColor(blockEntity));
        });
    }

    public int getColor(BlockEntity block){
        if (block instanceof ChestBlockEntity) return chestColor.get().getRgb();
        if (block instanceof ShulkerBoxBlockEntity) return shulkerColor.get().getRgb();
        if (block instanceof EnderChestBlockEntity) return echestColor.get().getRgb();
        if (block instanceof HopperBlockEntity) return hopperColor.get().getRgb();
        if (block instanceof FurnaceBlockEntity) return furnaceColor.get().getRgb();
        if (block instanceof BarrelBlockEntity) return barrelColor.get().getRgb();
        if (block instanceof BedBlockEntity) return bedColor.get().getRgb();
        if (block instanceof EndPortalBlockEntity) return endPortalColor.get().getRgb();
        return -1;
    }

    public Stream<BlockEntity> blockEntities(){
        return EntityUtils.getBlockEntities().stream().filter(blockEntity -> (chest.get() && blockEntity instanceof ChestBlockEntity)
        || (shulker.get() && blockEntity instanceof ShulkerBoxBlockEntity)
        || (echest.get() && blockEntity instanceof EnderChestBlockEntity)
        || (hopper.get() && blockEntity instanceof HopperBlockEntity)
        || (furnace.get() && blockEntity instanceof FurnaceBlockEntity)
        || (barrel.get() && blockEntity instanceof BarrelBlockEntity)
        || (bed.get() && blockEntity instanceof BedBlockEntity)
        || (endPortal.get() && blockEntity instanceof EndPortalBlockEntity));
    }


}
