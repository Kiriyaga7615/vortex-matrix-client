package cis.matrixclient.feature.module.modules.misc;

import cis.matrixclient.event.events.client.MouseButtonEvent;
import cis.matrixclient.feature.manager.FriendManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.key.KeyBinds;
import cis.matrixclient.util.player.FindItemResult;
import cis.matrixclient.util.player.InvUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

@Module.Info(name = "MiddleClick", category = Module.Category.MISC, drawn = true)
public class MiddleClick extends Module {
    public enum ItemMode{
        Pearl,
        Firework,
        Friend
    }

    public Setting<ItemMode> itemMode = register("Item", ItemMode.Pearl, ItemMode.values());

    private FindItemResult item;

    @Subscribe
    private void onMouseButton(MouseButtonEvent event){
        if (mc.currentScreen != null) return;
        if (event.action != KeyBinds.Action.PRESS || event.button != GLFW_MOUSE_BUTTON_MIDDLE) return;

        if (itemMode.get(ItemMode.Friend)){
            if (event.action != KeyBinds.Action.PRESS || event.button != GLFW_MOUSE_BUTTON_MIDDLE || mc.currentScreen != null || !(mc.targetedEntity instanceof PlayerEntity)) return;
            String name = mc.targetedEntity.getEntityName();

            if (FriendManager.isFriend(name)){
                FriendManager.removeFriend(name);
                info(name + " removed from friends");
            }
            else {
                FriendManager.addFriend(name);
                info(name + " added to friends");
            }
            return;
        }

        switch (itemMode.get()){
            case Pearl -> item = InvUtils.findInHotbar(Items.ENDER_PEARL);
            case Firework -> item = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof FireworkRocketItem);
        }

        if (!item.found()) {
            error("No item in your hotbar");
            return;
        }

        InvUtils.swap(item.slot(), true);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        InvUtils.swapBack();
    }


}
