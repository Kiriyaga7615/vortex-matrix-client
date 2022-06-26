package cis.matrixclient.feature.module.modules.player;

import cis.matrixclient.event.events.network.PacketEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.world.BlockUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;

@Module.Info(name = "NoInteract", category = Module.Category.PLAYER, drawn = true)
public class NoInteract extends Module {
    public Setting<Boolean> noBeds = register("NoBeds", true);
    public Setting<Boolean> noAnchors = register("NoAnchors", true);
    public Setting<Boolean> allowBlowUp = register("AllowBlowUp", true);

    public Setting<Boolean> noAnvils = register("NoAnvils", true);
    public Setting<Boolean> noEChest = register("NoEChest", false);
    public Setting<Boolean> noClickable = register("NoClickable", false);

    @Subscribe
    private void onPacket(PacketEvent.Send event){
        if(!(event.packet instanceof PlayerInteractBlockC2SPacket interactBlockC2SPacket)) return;

        Block block = BlockUtils.getBlock(interactBlockC2SPacket.getBlockHitResult().getBlockPos());

        if (noClickable.get() && BlockUtils.isClickable(block)){
            event.cancel();
        }
        if (noAnvils.get() && block instanceof AnvilBlock ||
                noEChest.get() && block instanceof EnderChestBlock){

            event.cancel();
        }
        if (noBeds.get() && block instanceof BedBlock) {
            if (allowBlowUp.get() && !mc.world.getDimension().bedWorks()) return;
            event.cancel();
        }
        if (noAnchors.get() && block instanceof RespawnAnchorBlock) {
            if (allowBlowUp.get() && !mc.world.getDimension().respawnAnchorWorks()) return;
            event.cancel();
        }
    }
}
