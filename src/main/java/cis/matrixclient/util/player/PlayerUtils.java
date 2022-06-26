package cis.matrixclient.util.player;

import cis.matrixclient.util.world.BlockUtils;
import net.minecraft.entity.Entity;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static cis.matrixclient.MatrixClient.mc;

public class PlayerUtils {
    public static double getTotalHealth() { return mc.player.getHealth() + mc.player.getAbsorptionAmount(); }
    public static double getHealth() { return mc.player.getHealth(); }

    public static int getFoodLevel() { return mc.player.getHungerManager().getFoodLevel(); }

    public static boolean shouldPause(boolean ifBreaking, boolean ifEating, boolean ifDrinking) {
        if (ifBreaking && mc.interactionManager.isBreakingBlock()) return true;
        if (ifEating && (mc.player.isUsingItem() && (mc.player.getMainHandStack().getItem().isFood() || mc.player.getOffHandStack().getItem().isFood()))) return true;
        return ifDrinking && (mc.player.isUsingItem() && (mc.player.getMainHandStack().getItem() instanceof PotionItem || mc.player.getOffHandStack().getItem() instanceof PotionItem));
    }

    public static double distanceTo(Entity entity) {
        return distanceTo(entity.getX(), entity.getY(), entity.getZ());
    }
    public static double distanceTo(BlockPos blockPos) { return distanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ()); }
    public static double distanceTo(Vec3d vec3d) {
        return distanceTo(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }
    public static double distanceTo(double x, double y, double z) {
        float f = (float) (mc.player.getX() - x);
        float g = (float) (mc.player.getY() - y);
        float h = (float) (mc.player.getZ() - z);
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

    public static boolean placeBlock(BlockPos pos, FindItemResult item){
        return placeBlock(pos, Hand.MAIN_HAND, item);
    }

    public static boolean placeBlock(BlockPos pos, Hand hand, FindItemResult item){
        if (pos == null || (!item.isOffhand() && item.isHotbar())) return false;

        if (hand == Hand.MAIN_HAND) mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(item.slot()));

        Direction direction = BlockUtils.getPlaceableSide(pos);
        if (direction == null) direction = Direction.DOWN;

        mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(BlockUtils.bestHitPos(pos), direction, pos, false));
        mc.player.swingHand(hand);
        return true;
    }

    public static boolean sendInteract(BlockPos pos, Hand hand, boolean packet){
        if (pos == null) return false;

        Direction direction = BlockUtils.getPlaceableSide(pos);
        if (direction == null) direction = Direction.DOWN;

        BlockHitResult hitResult = new BlockHitResult(BlockUtils.bestHitPos(pos), direction, pos, false);
        return sendInteract(hitResult, hand, packet);
    }

    public static boolean sendInteract(BlockHitResult result, Hand hand, boolean packet){
        if (packet) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, result, 0));
            mc.player.swingHand(hand);
        } else {
            mc.interactionManager.interactBlock(mc.player, hand, result);
            mc.player.swingHand(hand);
        }
        return true;
    }
}
