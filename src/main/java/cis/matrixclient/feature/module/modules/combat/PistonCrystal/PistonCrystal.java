package cis.matrixclient.feature.module.modules.combat.PistonCrystal;

import cis.matrixclient.event.events.render.Render3DEvent;
import cis.matrixclient.event.events.world.EntityAddedEvent;
import cis.matrixclient.event.events.world.EntityRemovedEvent;
import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.combat.PistonCrystal.positions.Positions;
import cis.matrixclient.feature.module.modules.combat.PistonCrystal.positions.Redstone;
import cis.matrixclient.feature.module.modules.combat.PistonCrystal.positions.Torch;
import cis.matrixclient.feature.module.modules.world.Timer;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.BlockPosX;
import cis.matrixclient.util.entity.TargetUtils;
import cis.matrixclient.util.math.TimerUtils;
import cis.matrixclient.util.player.FindItemResult;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.render.Renderer3D;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static cis.matrixclient.feature.module.modules.combat.PistonCrystal.PCUtils.*;
import static cis.matrixclient.util.player.InvUtils.*;

/**
 * @author Eureka
 */

@Module.Info(name = "PistonCrystal", category = Module.Category.COMBAT)
public class PistonCrystal extends Module {
    // Page
    public Page general = new Page("General");
    public Page delay = new Page("Delay");
    public Page placement = new Page("Placement");
    public Page pause = new Page("Pause");

    // General
    public Setting<Integer> targetRange = register("TargetRange", 7, 1, 10, general);
    public Setting<Double> placeRange = register("PlaceRange", 4.5, 1, 7, 1, general);
    public Setting<Break> doBreak = register("Break", Break.Packet, Break.values(), general);
    public Setting<Distance> distance = register("Distance", Distance.Closest, Distance.values(), general);

    // Delay
    public Setting<Integer> actionDelay = register("ActionDelay", 10, 0, 500, delay);
    public Setting<Integer> placeTries = register("PlaceTries", 1, 1, 5, delay);
    public Setting<Integer> delayedAttack = register("DelayedAttack", 25, 0, 100, delay);
    public Setting<Integer> ticksExisted = register("TicksExisted", 0, 0, 5, delay);
    public Setting<Integer> attackTries = register("AttackTries", 1, 1, 5, delay);
    public Setting<Double> timer = register("Timer", 1, 1, 2.5, 2, delay);

    // Placement
    public Setting<Activator> activator = register("Activator", Activator.Torch, Activator.values(), placement);
    public Setting<Boolean> strictDirection = register("StrictDirection", false, placement);
    public Setting<Boolean> allowUpper = register("AllowUpper", true, placement);
    public Setting<Boolean> trap = register("Trap", true, placement);

    // Pause
    public Setting<Boolean> toggleOnJump = register("ToggleOnJump", true, pause);
    public Setting<Boolean> pauseEat = register("PauseEat", true, pause);
    public Setting<Boolean> pauseCA = register("PauseCA", true, pause);

    // Render and Debug
    public Setting<Boolean> render = register("Render", true, general);
    public Setting<Color> color = register("Color", new Color(0.94f, 1f, 0.50f), general);
    public Setting<Boolean> debug = register("Debug", false, general);

    // Fields
    public PlayerEntity target;
    public EndCrystalEntity endCrystal;

    public BlockPosX crystalPos;
    public BlockPosX pistonPos;
    public BlockPosX blockPos;
    public BlockPosX trapPos;

    private Box crystalBox = null;
    private Box pistonBox = null;
    private Box blockPosBox = null;

    public Direction direction;

    private boolean shouldBreak;
    private int placed;
    private int attacks;

    public Stage stage;

    public Triplet currentTriplet;
    public Triplet stacked;

    public TimerUtils timerUtils = new TimerUtils();

    @Override
    public void onEnable() {
        crystalPos = null;
        pistonPos = null;
        blockPos = null;
        trapPos = null;

        shouldBreak = true;
        endCrystal = null;
        placed = 0;
        attacks = 0;

        stage = Stage.Preparing;

        currentTriplet = null;
        stacked = null;
        timerUtils.reset();
    }

    @Override
    public void onDisable() {
        Timer.instance.setOverride(1.0F);
    }

    @Subscribe
    public void onAdded(EntityAddedEvent event) {
        if (!(event.entity instanceof EndCrystalEntity)) return;
        if (crystalPos == null) return;

        if (crystalPos.equals(event.entity.getBlockPos())) this.endCrystal = (EndCrystalEntity) event.entity;
    }

    @Subscribe
    public void onRemove(EntityRemovedEvent event) {
        if (this.endCrystal == null) return;

        if (event.entity.equals(this.endCrystal)) {
            this.endCrystal = null;
            event.entity.kill();
        }
    }

    @Subscribe
    public void onTick(TickEvent.Post event) {
        target = TargetUtils.getPlayerTarget(targetRange.get(), TargetUtils.SortPriority.LowestDistance);
        if (TargetUtils.isBadTarget(target, targetRange.get())) {
            info("Can't find target, toggling...");
            toggle();
            return;
        }

        if (pauseEat.get() && mc.player.isUsingItem() && (mc.player.getMainHandStack().isFood() || mc.player.getOffHandStack().isFood()))
            return;
        if (toggleOnJump.get() && ((mc.options.jumpKey.isPressed() || mc.player.input.jumping) || mc.player.prevY < mc.player.getPos().getY())) {
            toggle();
            return;
        }

        doCheck();
        switch (stage) {
            case Preparing -> {
                if (getPositions(target).direction == Direction.UP) {
                    stacked = getPositions(target);
                    return;
                }

                currentTriplet = getPositions(target);
                crystalPos = getPositions(target).blockPos.get(0);
                direction = getPositions(target).direction;

                pistonPos = getPositions(target).blockPos.get(1);
                blockPos = getPositions(target).blockPos.get(2);
                trapPos = new BlockPosX(target.getBlockPos().up(2));

                if (trap.get() && canPlace(trapPos, false)) doPlace(findInHotbar(Items.OBSIDIAN), trapPos);

                stacked = null;
                stage = Stage.Piston;
            }
            case Piston -> {
                if (!timerUtils.passedMillis(actionDelay.get())) return;

                doPlace(findInHotbar(Items.PISTON, Items.STICKY_PISTON), pistonPos);
                nextStage(Stage.Crystal);
            }
            case Crystal -> {
                if (!timerUtils.passedMillis(actionDelay.get())) return;

                if (!hasEntity(new Box(crystalPos), entity -> entity instanceof EndCrystalEntity)) {
                    if (placed <= placeTries.get()) {
                        doPlace(findInHotbar(Items.END_CRYSTAL), crystalPos.down());
                        placed++;

                        debug("PlaceTries: " + placed + ";");
                    }
                } else nextStage(Stage.Block);
            }
            case Block -> {
                if (!timerUtils.passedMillis(actionDelay.get())) return;

                doPlace(activator.get(Activator.Torch) ? findInHotbar(Items.REDSTONE_TORCH) : findInHotbar(Items.REDSTONE_BLOCK), blockPos);
                nextStage(Stage.Attack);
            }
            case Attack -> {
                if (doBreak.get(Break.Packet) && activator.get(Activator.Redstone)) doBreak(blockPos);
                if (!timerUtils.passedMillis(actionDelay.get() + delayedAttack.get())) return;
                if (endCrystal != null && endCrystal.age <= ticksExisted.get()) return;

                if (this.endCrystal != null) {
                    if (attacks <= attackTries.get()) {
                        doAttack(endCrystal);
                        attacks++;

                        debug("AttackTries: " + attacks + ";");
                    }
                } else nextStage(Stage.BreakBlock);
            }
            case BreakBlock -> {
                if (doBreak.get(Break.Client) || activator.get(Activator.Torch)) doBreak(blockPos);
                if (this.endCrystal == null) swap(findFastestToolInHotbar(mc.world.getBlockState(blockPos)));
                mc.player.swingHand(Hand.MAIN_HAND);

                if (canPlace(blockPos, true)) {
                    shouldBreak = true;

                    if (Positions.isUpper()) nextStage(Stage.BreakPiston);
                    else nextStage(Stage.Preparing);
                }
            }
            case BreakPiston -> {
                doBreak(pistonPos);
                swap(findFastestToolInHotbar(mc.world.getBlockState(pistonPos)));
                mc.player.swingHand(Hand.MAIN_HAND);

                if (canPlace(pistonPos, false)) {
                    shouldBreak = true;

                    nextStage(Stage.Preparing);
                }
            }
        }
    }

    private void nextStage(Stage stage) {
        this.stage = stage;
        timerUtils.reset();

        placed = 0;
        attacks = 0;
    }

    private void doPlace(FindItemResult itemResult, BlockPosX blockPos) {
        if (blockPos == null) return;
        if (!itemResult.found()) return;
        Hand hand = itemResult.isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND;
        boolean isTorch = itemResult.equals(findInHotbar(Items.REDSTONE_TORCH));

        if (!itemResult.isOffhand()) mc.player.getInventory().selectedSlot = itemResult.slot();

        doRotate(() -> {
            mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(closestVec3d(blockPos.getBoundingBox()), Direction.DOWN, blockPos, false));
            mc.player.swingHand(hand);
        }, isTorch, blockPos);
    }

    private void doBreak(BlockPosX blockPos) {
        if (!canBreak(blockPos)) return;

        if (doBreak.get(Break.Packet)) {
            if (!shouldBreak) return;

            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.DOWN));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.DOWN));
            shouldBreak = false;
        } else mc.interactionManager.updateBlockBreakingProgress(blockPos, Direction.DOWN);
    }

    private void doAttack(EndCrystalEntity endCrystal) {
        if (endCrystal == null) return;
        HitResult prevResult = mc.crosshairTarget;

        mc.crosshairTarget = new EntityHitResult(endCrystal, closestVec3d(endCrystal.getBoundingBox()));
        leftClick();
        mc.crosshairTarget = prevResult;
    }

    private void doRotate(Runnable callback, boolean isTorch, BlockPosX blockPos) {
        if (this.direction == null) return;
        float yaw = isTorch ? getYaw(torchDirection(blockPos)) : (strictDirection.get() ? mc.player.getYaw() : getYaw(this.direction));
        float pitch = isTorch && torchDirection(blockPos) == Direction.DOWN ? 90 : 0;

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
        callback.run();
    }

    private Direction torchDirection(BlockPosX blockPos) {
        if (!blockPos.down().isAir()) return Direction.DOWN;

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP) continue;
            BlockPosX offset = blockPos.offset(direction);

            if (offset.isOf(PistonBlock.class)) continue;
            if (offset.isAir()) continue;

            return direction;
        }

        return Direction.UP;
    }

    private void doCheck() {
        if (!findInHotbar(Items.END_CRYSTAL).found() || !findInHotbar(Items.PISTON, Items.STICKY_PISTON).found() || !findInHotbar(activator.get(Activator.Torch) ? Items.REDSTONE_TORCH : Items.REDSTONE_BLOCK).found() || !findInHotbar(Items.NETHERITE_PICKAXE, Items.DIAMOND_PICKAXE).found()) {
            info("Can't find required items, toggling...");
            toggle();
            return;
        } else Timer.instance.setOverride(timer.get().floatValue());
    }

    private Triplet getPositions(PlayerEntity target) {
        Positions positions = activator.get(Activator.Torch) ? new Torch() : new Redstone();
        List<Triplet> init = positions.init(target);

        for (Triplet triplet : init) {
            if (positions.canPlace(triplet)) return triplet;
        }

        return new Triplet(init.get(0).blockPos, Direction.UP);
    }

    private boolean canBreak(BlockPosX blockPos) {
        return !blockPos.isAir();
    }

    public boolean canPlace(BlockPosX blockPos, boolean ignoreEntity) {
        if (blockPos == null) return false;
        if (!blockPos.isAir()) return false;
        if (ignoreEntity) return true;

        return !hasEntity(new Box(blockPos), entity -> entity instanceof PlayerEntity || entity instanceof EndCrystalEntity || entity instanceof TntEntity);
    }

    public boolean shouldPause() {
        return isEnabled() && pauseCA.get();
    }

//    public boolean shouldReturn(BlockPosX blockPos) {
//        return isEnabled() && currentTriplet != null && currentTriplet.blockPos.contains(blockPos);
//    }

    private void debug(String text) {
        if (!debug.get()) return;

        info(text);
    }

    @Subscribe
    public void onRender(Render3DEvent event) {
        if (!render.get()) return;
        if (crystalPos == null || pistonPos == null || blockPos == null || (trap.get() && trapPos == null)) return;

        // Crystal
        Box postCrystal = new Box(crystalPos);
        if (crystalBox == null) crystalBox = postCrystal;

        double xC = (postCrystal.minX - crystalBox.minX) / 10;
        double yC = (postCrystal.minY - crystalBox.minY) / 10;
        double zC = (postCrystal.minZ - crystalBox.minZ) / 10;

        crystalBox = new Box(crystalBox.minX + xC, crystalBox.minY + yC, crystalBox.minZ + zC, crystalBox.maxX + xC, crystalBox.maxY + yC, crystalBox.maxZ + zC);
        Vec3d crystalVec3d = Renderer3D.getRenderPosition(crystalBox.getCenter().x - 0.5, crystalBox.getCenter().y - 0.5, crystalBox.getCenter().getZ() - 0.5);
        postCrystal = new Box(crystalVec3d.x, crystalVec3d.y, crystalVec3d.z, crystalVec3d.x + 1.0, crystalVec3d.y + 1.0, crystalVec3d.z + 1.0);

        Renderer3D.get.drawOutlineBox(event.getMatrixStack(), postCrystal, color.get().getRgb());

        // Piston
        Box postPiston = new Box(pistonPos);
        if (pistonBox == null) pistonBox = postPiston;

        double xP = (postPiston.minX - pistonBox.minX) / 10;
        double yP = (postPiston.minY - pistonBox.minY) / 10;
        double zP = (postPiston.minZ - pistonBox.minZ) / 10;

        pistonBox = new Box(pistonBox.minX + xP, pistonBox.minY + yP, pistonBox.minZ + zP, pistonBox.maxX + xP, pistonBox.maxY + yP, pistonBox.maxZ + zP);
        Vec3d pistonVec3d = Renderer3D.getRenderPosition(pistonBox.getCenter().x - 0.5, pistonBox.getCenter().y - 0.5, pistonBox.getCenter().getZ() - 0.5);
        postPiston = new Box(pistonVec3d.x, pistonVec3d.y, pistonVec3d.z, pistonVec3d.x + 1.0, pistonVec3d.y + 1.0, pistonVec3d.z + 1.0);

        Renderer3D.get.drawOutlineBox(event.getMatrixStack(), postPiston, color.get().getRgb());

        // Redstone
        Box postBlockPos = new Box(blockPos);
        if (blockPosBox == null) blockPosBox = postBlockPos;

        double xR = (postBlockPos.minX - blockPosBox.minX) / 10;
        double yR = (postBlockPos.minY - blockPosBox.minY) / 10;
        double zR = (postBlockPos.minZ - blockPosBox.minZ) / 10;

        blockPosBox = new Box(blockPosBox.minX + xR, blockPosBox.minY + yR, blockPosBox.minZ + zR, blockPosBox.maxX + xR, blockPosBox.maxY + yR, blockPosBox.maxZ + zR);
        Vec3d blockPosVec3d = Renderer3D.getRenderPosition(blockPosBox.getCenter().x - 0.5, blockPosBox.getCenter().y - 0.5, blockPosBox.getCenter().getZ() - 0.5);
        postBlockPos = new Box(blockPosVec3d.x, blockPosVec3d.y, blockPosVec3d.z, blockPosVec3d.x + 1.0, blockPosVec3d.y + 1.0, blockPosVec3d.z + 1.0);

        Renderer3D.get.drawOutlineBox(event.getMatrixStack(), postBlockPos, color.get().getRgb());

        // Trap
        Renderer3D.get.drawBoxWithOutline(event.getMatrixStack(), trap.get() && canPlace(trapPos, false) ? trapPos : null, color.get().getRgb());
    }

    public enum Break {
        Packet, Client
    }

    public enum Distance {
        Closest, Highest
    }

    public enum Activator {
        Redstone, Torch
    }

    public enum Stage {
        Preparing, Piston, Crystal, Block, Attack, BreakBlock, BreakPiston
    }

    public static PistonCrystal instance;

    public PistonCrystal() {
        instance = this;
    }
}