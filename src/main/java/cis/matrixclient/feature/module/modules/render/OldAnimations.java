package cis.matrixclient.feature.module.modules.render;

import cis.matrixclient.event.events.render.HeldItemRendererEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.mixins.IHeldItemRendererAccessor;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

@Module.Info(name = "OldAnimations", category = Module.Category.RENDER)
public class OldAnimations extends Module {
    public Setting<Boolean> swap = register("Swap", false);

    private static int slotMainHand = 0;

    @Subscribe
    public void onUpdateHeldItem(HeldItemRendererEvent event) {
        event.cancel();

        IHeldItemRendererAccessor heldItemRenderer = ((IHeldItemRendererAccessor) mc.getEntityRenderDispatcher().getHeldItemRenderer());
        ItemStack mainHandStack = mc.player.getMainHandStack();
        ItemStack offHandStack = mc.player.getOffHandStack();

        heldItemRenderer.setPrevEquipProgressMainHand(heldItemRenderer.getEquipProgressMainHand());
        heldItemRenderer.setPrevEquipProgressOffHand(heldItemRenderer.getEquipProgressOffHand());

        if (mc.player.isRiding()) {
            heldItemRenderer.setEquipProgressMainHand(MathHelper.clamp(heldItemRenderer.getEquipProgressMainHand() - 0.4F, 0.0F, 1.0F));
            heldItemRenderer.setEquipProgressOffHand(MathHelper.clamp(heldItemRenderer.getEquipProgressOffHand() - 0.4F, 0.0F, 1.0F));
        } else {
            boolean reequipM = swap.get() && shouldCauseReequipAnimation(heldItemRenderer.getMainHand(), mainHandStack, mc.player.getInventory().selectedSlot);
            boolean reequipO = swap.get() && shouldCauseReequipAnimation(heldItemRenderer.getOffHand(), offHandStack, -1);
            if (!reequipM && !Objects.equals(heldItemRenderer.getMainHand(), mainHandStack)) heldItemRenderer.setMainHand(mainHandStack);
            if (!reequipO && !Objects.equals(heldItemRenderer.getMainHand(), offHandStack)) heldItemRenderer.setOffHand(offHandStack);
            heldItemRenderer.setEquipProgressMainHand(heldItemRenderer.getEquipProgressMainHand() + MathHelper.clamp((!reequipM ? 1.0F : 0.0F) - heldItemRenderer.getEquipProgressMainHand(), -0.4F, 0.4F));
            heldItemRenderer.setEquipProgressOffHand(heldItemRenderer.getEquipProgressOffHand() + MathHelper.clamp((!reequipO ? 1.0F : 0.0F) - heldItemRenderer.getEquipProgressOffHand(), -0.4F, 0.4F));
        }
        if (heldItemRenderer.getPrevEquipProgressMainHand() < 0.1F) heldItemRenderer.setMainHand(mainHandStack);
        if (heldItemRenderer.getEquipProgressOffHand() < 0.1F) heldItemRenderer.setOffHand(offHandStack);
    }

    private boolean shouldCauseReequipAnimation(ItemStack from, ItemStack to, int slot) {
        boolean fromInvalid = from.isEmpty();
        boolean toInvalid = to.isEmpty();
        if (fromInvalid && toInvalid) return false;
        if (fromInvalid || toInvalid) return true;
        if (slot != -1) slotMainHand = slot;
        return !from.equals(to);
    }
}