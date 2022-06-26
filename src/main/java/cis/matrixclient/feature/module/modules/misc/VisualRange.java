package cis.matrixclient.feature.module.modules.misc;

import cis.matrixclient.event.events.world.EntityAddedEvent;
import cis.matrixclient.event.events.world.EntityRemovedEvent;
import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.manager.FriendManager;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.client.FakePlayer;
import cis.matrixclient.feature.setting.Setting;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

@Module.Info(name = "VisualRange", category = Module.Category.MISC, drawn = true)
public class VisualRange extends Module {
    public Setting<Boolean> onAdd = register("OnAdd", true);
    public Setting<Boolean> onRemove = register("OnRemove", true);

    public Setting<Boolean> ignoreFriends = register("IgnoreFriends", true);
    public Setting<Boolean> ignoreFakePlayers = register("IgnoreFakePlayer", true);

    public Setting<Boolean> sendLastCoords = register("SendLastCoords", false);
    public FakePlayer fakePlayer = ModuleManager.getModule(FakePlayer.class);

    private HashMap<PlayerEntity, Integer> map = new HashMap<>();

    @Subscribe
    private void onAdd(EntityAddedEvent event){
        Entity entity = event.entity;
        if (entity.getUuid() == mc.player.getUuid() || !onAdd.get()) return;

        if (event.entity instanceof PlayerEntity player){
            if (ignoreFriends.get() && FriendManager.isFriend(player.getEntityName())) return;
            if (ignoreFakePlayers.get() & fakePlayer != null  && player == fakePlayer.fakePlayer) return;
            info(Formatting.GREEN + entity.getEntityName() + Formatting.GRAY + " came to get fucked" + Formatting.GOLD + (sendLastCoords.get() ? " [" + player.getBlockPos().getX() + " " + player.getBlockPos().getY() + " " + player.getBlockPos().getZ() + "]" : ""));
        }
    }

    @Subscribe
    private void onRemove(EntityRemovedEvent event){
        Entity entity = event.entity;
        if (entity.getUuid() == mc.player.getUuid() || !onRemove.get()) return;
        if (event.entity instanceof PlayerEntity player){
            if (ignoreFriends.get() && FriendManager.isFriend(player.getEntityName())) return;
            if (ignoreFakePlayers.get() & fakePlayer != null && player == fakePlayer.fakePlayer) return;

            map.put(player, 3);
        }
    }

    @Subscribe
    private void onTick(TickEvent event){
        for (Map.Entry<PlayerEntity, Integer> entry : map.entrySet()) {
            PlayerEntity player = entry.getKey();
            Integer integer = entry.getValue();
            if (integer > 0) map.replace(player, integer - 1);
            else {
                if (mc.getNetworkHandler().getPlayerList().stream().noneMatch(playerListEntry -> playerListEntry.getProfile() == player.getGameProfile())) {
                    info(Formatting.RED + player.getEntityName() + Formatting.GRAY + " left because he was weak" + Formatting.GOLD + (sendLastCoords.get() ? " [" + player.getBlockPos().getX() + " " + player.getBlockPos().getY() + " " + player.getBlockPos().getZ() + "]" : ""));
                } else {
                    info(Formatting.DARK_PURPLE + player.getEntityName() + Formatting.GRAY + " got scared" + Formatting.GOLD + (sendLastCoords.get() ? " [" + player.getBlockPos().getX() + " " + player.getBlockPos().getY() + " " + player.getBlockPos().getZ() + "]" : ""));
                }
                map.remove(player);
            }
        }
    }
}
