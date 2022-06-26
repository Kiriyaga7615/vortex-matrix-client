package cis.matrixclient.feature.module.modules.combat.HoleFill;

import cis.matrixclient.util.BlockPosX;
import cis.matrixclient.util.player.InvUtils;
import cis.matrixclient.util.player.Rotations;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import static cis.matrixclient.MatrixClient.mc;

public class HFUtils {
    public static double distanceTo(BlockPosX blockPos1, BlockPosX blockPos2) {
        double d = blockPos1.getX() - blockPos2.getX();
        double e = blockPos1.getY() - blockPos2.getY();
        double f = blockPos1.getZ() - blockPos2.getZ();
        return MathHelper.sqrt((float) (d * d + e * e + f * f));
    }

    public static void place(BlockPosX pos, boolean rotate, int slot) {
        if (pos != null) {
            int prevSlot = mc.player.getInventory().selectedSlot;
            InvUtils.swap(slot, false);
            if (rotate) Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos));
            BlockHitResult result = new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.DOWN, pos, true);
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, 0));
            mc.player.swingHand(Hand.MAIN_HAND);
            InvUtils.swap(prevSlot, false);
        }
    }
}
