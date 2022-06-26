package cis.matrixclient.feature.module.modules.combat;

import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.BlockPosX;
import cis.matrixclient.util.player.FindItemResult;
import cis.matrixclient.util.player.InvUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Module.Info(name = "SelfFill", category = Module.Category.COMBAT)
public class SelfFill extends Module {
    public Setting<Move> move = register("Move", Move.Packet, Move.values());
    public Setting<BlockMode> block = register("Block", BlockMode.Anvil, BlockMode.values());
    public Setting<Double> rubberband = register("RubberBand", 7, 1, 7, 1);

    private FindItemResult result;

    @Subscribe
    public void onTick(TickEvent.Post event) {
        BlockPosX blockPos = new BlockPosX(mc.player.getBlockPos());
        switch (block.get()) {
            case Anvil -> result = InvUtils.findInHotbar(Items.ANVIL);
            case Obsidian -> result = InvUtils.findInHotbar(Items.OBSIDIAN);
            case EChest -> result = InvUtils.findInHotbar(Items.ENDER_CHEST);
        }

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.4, mc.player.getZ(), false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.75, mc.player.getZ(), false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.01, mc.player.getZ(), false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.15, mc.player.getZ(), false));

        if (!blockPos.isReplaceable()) {
            info("Already burrowed, disabling...");
            toggle();
            return;
        }

        InvUtils.swap(result.slot(), true);
        doPlace(blockPos);
        InvUtils.swapBack();

        switch (move.get()) {
            case Packet -> mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + rubberband.get(), mc.player.getZ(), false));
            case Client -> mc.player.updatePosition(mc.player.getX(), mc.player.getY() + rubberband.get(), mc.player.getZ());
        }

        toggle();
    }

    public void doPlace(BlockPosX blockPos) {
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Direction.UP, blockPos, false));
        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
    }

    public enum Move {
        Packet, Client
    }

    public enum BlockMode {
        Anvil, Obsidian, EChest
    }
}