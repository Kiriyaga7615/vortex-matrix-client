package cis.matrixclient.feature.module.modules.world.MiningTS;

import cis.matrixclient.event.events.player.StartBreakingBlockEvent;
import cis.matrixclient.event.events.render.Render3DEvent;
import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.BlockPosX;
import cis.matrixclient.util.entity.TargetUtils;
import cis.matrixclient.util.math.TimerUtils;
import cis.matrixclient.util.player.FindItemResult;
import cis.matrixclient.util.player.InvUtils;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.render.Renderer3D;
import com.google.common.eventbus.Subscribe;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

import static cis.matrixclient.feature.module.modules.world.MiningTS.MTSUtils.*;

@Module.Info(name = "MiningTS", category = Module.Category.WORLD)
public class MiningTS extends Module {
    public Setting<Boolean> autoCity = register("AutoCity", false);
    public Setting<Pickaxe> pickaxe = register("Pickaxe", Pickaxe.NoDrop, Pickaxe.values());
    public Setting<Integer> actionDelay = register("ActionDelay", 0, 0, 5);
    public Setting<Boolean> limit = register("Limit", false);
    public Setting<Integer> limitBreak = register("LimitBreak", 1, 1, 10);
    public Setting<Boolean> surroundOnly = register("SurroundOnly", false);
    public Setting<Boolean> fastBreak = register("FastBreak", false);
    public Setting<Boolean> haste = register("Haste", false);
    public Setting<Integer> amplifier = register("HasteAmplifier", 2, 1, 3);
    public Setting<Boolean> ignoreAir = register("IgnoreAir", true);
    public Setting<Boolean> swing = register("Swing", true);

    public Setting<Color> color = register("Color", new Color(0.94f, 1f, 0.50f));

    private PlayerEntity target;

    private BlockPosX blockPos = null;
    private Direction direction = null;

    private int breakTimes;

    private final TimerUtils timer = new TimerUtils();

    @Override
    public void onEnable() {
        breakTimes = 0;

        if (autoCity.get()) {
            target = TargetUtils.getPlayerTarget(6.5, TargetUtils.SortPriority.LowestDistance);
            if (TargetUtils.isBadTarget(target, 6)) {
                info("Target not found, ignoring...");
            } else {
                List<BlockPosX> blocks = getBlocksAround(target);
                blocks = blocks.stream().filter(blockPos -> {
                    if (blockPos.isOf(Blocks.BEDROCK)) return false;
                    return !blockPos.isAir();
                }).sorted(Comparator.comparingDouble(MTSUtils::distanceTo)).toList();

                if (blocks.isEmpty()) {
                    info("Vulnerable positions is empty, ignoring...");
                } else {
                    blockPos = blocks.get(0);
                    direction = mc.player.getY() > blockPos.getY() ? Direction.UP : Direction.DOWN;

                    int slot = pickSlot();
                    if (slot == 420) {
                        info("Pickaxe not found.");
                        return;
                    }

                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, direction));
                    if (swing.get()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

                    progress = 0;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        blockPos = null;
        direction = null;
        if (mc.player.hasStatusEffect(StatusEffects.HASTE)) mc.player.removeStatusEffect(StatusEffects.HASTE);
    }

    @Subscribe
    public void onStartBreaking(StartBreakingBlockEvent event) {
        BlockPosX blockPos = new BlockPosX(event.blockPos);

        if (!blockPos.isBreakable()) return;
        if (surroundOnly.get() && !isPlayerNear(blockPos)) return;

        this.blockPos = blockPos;
        this.direction = event.direction;

        breakTimes = 0;
        progress = 0;
    }


    @Subscribe
    public void onTick(TickEvent.Post event) {
        if (blockPos == null) return;
        if (haste.get()) addhaste(mc.player);
        int slot = pickSlot();
        if (!canBreak(slot, blockPos)) {
            timer.reset();
        } else swap(slot);
    }

    private boolean swap(int slot) {
        if (slot == 420 || progress < 1 || (ignoreAir.get() && blockPos.isAir()) || (limit.get() && breakTimes >= limitBreak.get()) || !timer.passedTicks(actionDelay.get()))
            return false;

        move(mc.player.getInventory().selectedSlot, slot);
        mine(blockPos);
        move(mc.player.getInventory().selectedSlot, slot);
        timer.reset();
        return true;
    }

    private int pickSlot() {
        FindItemResult pick = pickaxe.get(Pickaxe.Fastest) ? InvUtils.findFastestTool(blockPos.getState()) : InvUtils.find(Items.GOLDEN_PICKAXE, Items.IRON_PICKAXE);
        return pick.found() ? pick.slot() : 420;
    }

    private void move(int from, int to) {
        ScreenHandler handler = mc.player.currentScreenHandler;

        Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap<>();
        stack.put(to, handler.getSlot(to).getStack());

        mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId, handler.getRevision(), PlayerInventory.MAIN_SIZE + from, to, SlotActionType.SWAP, handler.getCursorStack().copy(), stack));
    }

    private void mine(BlockPosX blockPos) {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
        if (swing.get()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

        if (fastBreak.get()) blockPos.setState(Blocks.AIR);
        breakTimes++;
    }

    private void addhaste(PlayerEntity player) {
        if (!mc.player.hasStatusEffect(StatusEffects.HASTE)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 255, amplifier.get() - 1, false, false, true));
        }
    }


    @Subscribe
    public void onRender(Render3DEvent event) {
        if (blockPos == null) return;
        if (limit.get() && breakTimes >= limitBreak.get()) return;

        int slot = pickSlot();
        if (slot == 420) return;

        double min = progress / 2;
        Vec3d vec3d = blockPos.getCenter();
        Vec3d fixedVec = Renderer3D.getRenderPosition(vec3d);
        Box box = new Box(fixedVec.x - min, fixedVec.y - min, fixedVec.z - min, fixedVec.x + min, fixedVec.y + min, fixedVec.z + min);
        Renderer3D.get.drawBox(event.getMatrixStack(), box, color.get().getRgb());
    }

    public enum Pickaxe {
        Fastest, NoDrop
    }
}
