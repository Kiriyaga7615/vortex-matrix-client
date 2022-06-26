package cis.matrixclient.feature.module.modules.movement;

import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.Keybind;
import cis.matrixclient.util.player.ChatUtils;
import cis.matrixclient.util.player.FindItemResult;
import cis.matrixclient.util.player.InvUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@Module.Info(name = "AutoJump", category = Module.Category.MOVEMENT, drawn = true)
public class AutoJump extends Module {
    public Page elytraPage = new Page("Elytra");
    public enum Mode {
        Jump,
        Elytra
    }

    public Setting<Mode> mode = register("Mode", Mode.Jump, Mode.values());

    // public Setting<Boolean> checkAbove = register("CheckAbove", true);
    public Setting<Boolean> startFallFlying = register("StartFallFlying", false, elytraPage);
    public Setting<Keybind> keybind = register("StartBind", Keybind.none(), elytraPage);
    public Setting<Integer> pitch = register("Pitch", -60, -90, 0, elytraPage);

    private boolean wasFireWork;
    private FindItemResult firework;
    private BlockPos blockPos;

    @Subscribe
    private void onTick(TickEvent.Post event) {
        blockPos = mc.player.getBlockPos();
        switch (mode.get()) {
            case Jump -> {
                if (mc.player.isOnGround()) mc.player.jump();
            }
            case Elytra -> {
                if (!mc.player.isFallFlying() || mc.player.isOnGround()) wasFireWork = false;
                firework = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof FireworkRocketItem);

                if (!firework.isHotbar()) {
                    ChatUtils.error("Cant find fireworks! Disabling..");
                    toggle();
                    return;
                }
                if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
                    ChatUtils.error("Ur Chest is not an Elytra! Disabling...");
                    toggle();
                    return;
                }
                /*
                if (checkAbove.get() && !mc.world.isAir(blockPos.up(2))) {
                    ChatUtils.error("Block Above u! Disabling...");
                    toggle();
                    return;
                }
                 */
                if (startFallFlying.get() && keybind.get().isPressed()) {

                    if (mc.player.isOnGround()) {
                        mc.player.setPitch(pitch.get());
                        mc.player.jump();
                    } else if (!mc.player.isFallFlying())
                        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

                    if (mc.player.isFallFlying() && !wasFireWork) {
                        useFirework(firework);
                    }
                }
            }
        }
    }

    private void useFirework(FindItemResult firework) {
        if (!firework.isMain()) InvUtils.swap(firework.slot(), true);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        InvUtils.swapBack();
        wasFireWork = true;
    }
}
