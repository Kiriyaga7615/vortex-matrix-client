package cis.matrixclient.feature.module.modules.combat.BowSpam;

import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.world.Timer;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.player.FindItemResult;
import cis.matrixclient.util.player.InvUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@Module.Info(name = "BowSpam", category = Module.Category.COMBAT)
public class BowSpam extends Module {
    // TODO: 16.05.2022 режим который не работает если есть только стерлы на хорошие еффекты + перекладывает местами есть есть обычные/инстант дамаг 1/2
    public Setting<Integer> actionDelay = register("ActionDelay", 5, 0, 10);
    public Setting<Boolean> onRightClick = register("OnRightClick", true);
    public Setting<Boolean> timer = register("Timer", true);
    public Setting<Double> factor = register("Factor", 1.4, 1, 2, 1);
    public Setting<Boolean> onGround = register("OnGround", true);
    public Setting<Boolean> autoSwap = register("AutoSwap", false);

    private boolean shouldSwap;
    private int prevSlot;

    @Override
    public void onEnable() {
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        mc.options.useKey.setPressed(false);
        Timer.instance.setOverride(1.0F);
    }

    @Subscribe
    public void onTick(TickEvent.Post event) {
        if (onGround.get() && !mc.player.isOnGround()) return;
        if (timer.get() && mc.options.useKey.isPressed()) Timer.instance.setOverride(factor.get().floatValue());

        doSwap();
        if (holdingInfo() != null) {
            if (mc.player.getItemUseTime() < actionDelay.get()) return;
            if (!onRightClick.get()) mc.options.useKey.setPressed(true);

            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(holdingInfo(), 0));

            mc.player.clearActiveItem();
        } else Timer.instance.setOverride(1.0F);
    }

    private Hand holdingInfo() {
        if (mc.player.getOffHandStack().getItem() != Items.BOW && mc.player.getMainHandStack().getItem() != Items.BOW) return null;

        return mc.player.getMainHandStack().getItem() != Items.BOW ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    private void doSwap() {
        if (!autoSwap.get()) return;

        if (mc.options.useKey.isPressed()) {
            if (holdingInfo() != null) return;

            FindItemResult bow = InvUtils.findInHotbar(Items.BOW);
            if (!bow.found()) {
                info("Can't find bow, toggling...");
                toggle();
                return;
            }

            prevSlot = mc.player.getInventory().selectedSlot;
            InvUtils.swap(bow);
            shouldSwap = true;
        } else if (prevSlot != -1 && shouldSwap) {
            InvUtils.swap(prevSlot, false);
            shouldSwap = false;
        }
    }
}
