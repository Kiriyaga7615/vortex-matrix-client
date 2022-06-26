package cis.matrixclient.feature.module.modules.combat;

import cis.matrixclient.event.events.game.GameJoinedEvent;
import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.player.DamageUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Objects;

@Module.Info(name = "AutoTotem", category = Module.Category.COMBAT)
public class AutoTotem extends Module {

    public Setting<Boolean> strict = register("Strict", false);
    public Setting<Integer> health = register("Health", 15, 1, 36);
    public Setting<Integer> ping = register("Ping", 200, 100, 1500);
    public Setting<Boolean> onLag = register("OnServerLag", false);
    public Setting<Boolean> onFall = register("onFalling", true);
    public Setting<Boolean> hotbarExclude = register("HotbarExclude", true);
    //public Setting<Boolean> containerSafety = register("ContainerSafety", false);

    // TODO: 07.05.2022 onlag sloman
    private long timeLastTimeUpdate = -1;
    private long timeGameJoined;

    @Subscribe
    public void onTick(TickEvent.Post event) {
        if (mc.player.currentScreenHandler instanceof CreativeInventoryScreen.CreativeScreenHandler) return;

        if (mc.player.getOffHandStack().getItem() == item()) return;

        /*/
        if (item() == Items.TOTEM_OF_UNDYING && containerSafety.get() && mc.player.currentScreenHandler instanceof GenericContainerScreenHandler){
            mc.options.inventoryKey.setPressed(true);
            mc.options.inventoryKey.setPressed(false);
            ChatUtils.info("s");
        }

         */

        if (mc.player.currentScreenHandler.getCursorStack().getItem() == item()) {
            if (!(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler))
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
            return;
        }

        int idx = -1;
        int id = -1;
        for (int i = 0; i < 36; ++i) {
            ItemStack itemstack;
            int id2 = Idx2Id(i, mc.player.currentScreenHandler);
            if (id2 < 0 || id2 >= mc.player.playerScreenHandler.slots.size() || (itemstack = (mc.player.playerScreenHandler.slots.get(id2)).getStack()).isEmpty() || itemstack.getItem() != item())
                continue;
            if (hotbarExclude.get() && Idx2Id(i, mc.player.currentScreenHandler) >= 36 && Idx2Id(i, mc.player.currentScreenHandler) <= 44)
                continue;

            idx = i;
            break;
        }

        if (idx == -1 && !(mc.player.currentScreenHandler instanceof PlayerScreenHandler)) {
            for (Slot slot : mc.player.currentScreenHandler.slots) {
                if (slot.getStack().isEmpty() || slot.getStack().getItem() != item()) continue;
                id = slot.id;
                break;
            }
        }

        if (id == -1) {
            if (idx == -1) {
                return;
            }
            id = Idx2Id(idx, mc.player.currentScreenHandler);
        }
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, 40, SlotActionType.SWAP, mc.player);
    }

    @Subscribe
    private void onGameJoined(GameJoinedEvent event) {
        timeGameJoined = timeLastTimeUpdate = System.currentTimeMillis();
    }

    private float getTimeSinceLastTick() {
        long now = System.currentTimeMillis();
        if (now - timeGameJoined < 4000) return 0;
        return (now - timeLastTimeUpdate) / 1000f;
    }

    private Item item() {
        if (strict.get() || mc.player.isFallFlying() || getHealth() - possibleHealthReductions(true,onFall.get()) < health.get() || getTimeSinceLastTick() > 1 && onLag.get())
            return Items.TOTEM_OF_UNDYING;

        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (playerListEntry != null & playerListEntry.getLatency() > ping.get()){
            return Items.TOTEM_OF_UNDYING;
        }
        if (totemOverride != null) return totemOverride;
        return Items.END_CRYSTAL;
    }

    public static Item totemOverride;
    public static float getHealth() {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }

    public static int Idx2Id(int idx, ScreenHandler handler) {
        if (handler instanceof PlayerScreenHandler) {
            if (PlayerInventory.isValidHotbarIndex(idx)) {
                return idx + 36;
            }
            if (IsMain(idx)) {
                return idx;
            }
        } else {
            if (PlayerInventory.isValidHotbarIndex(idx)) {
                return idx + handler.slots.size() - 9;
            }
            if (IsMain(idx)) {
                return idx + handler.slots.size() - 45;
            }
        }
        return -1;
    }

    private static boolean IsMain(int i) {
        return i >= 9 && i < 36;
    }
    public static void totemItem(Item item){
        totemOverride = item;
    }
    public static void totemItem(){
        totemOverride = null;
    }

    public static double possibleHealthReductions(boolean entities, boolean fall) {
        double damageTaken = 0;
        if (entities) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof EndCrystalEntity && damageTaken < DamageUtils.crystalDamage(mc.player, entity.getPos())) {
                    damageTaken = DamageUtils.crystalDamage(mc.player, entity.getPos());
                }
            }
        }
        if (fall) {
            if (mc.player.fallDistance > 3) {
                double damage = mc.player.fallDistance * 0.5;

                if (damage > damageTaken) {
                    damageTaken = damage;
                }
            }
        }
        return damageTaken;
    }

    @Override
    public String getInfo() {
        return strict.get() ? "Strict" : "Smart";
    }
}