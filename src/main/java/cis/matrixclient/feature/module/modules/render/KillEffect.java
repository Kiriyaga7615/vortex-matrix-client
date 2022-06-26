package cis.matrixclient.feature.module.modules.render;

import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Random;

@Module.Info(name = "KillEffect", category = Module.Category.RENDER, drawn = true)
public class KillEffect extends Module {
    public Page thunderPage = new Page("Thunder");

    public Setting<Boolean> thunder = register("Thunder", true);
    public Setting<Integer> thunderCount = register("ThunderCount", 1, 1, 100);
    public Setting<Double> scale = register("Scale", 0.1, 0.01, 1, 2);
    public Setting<Double> scaleY = register("ScaleY", 0.1, 0.01, 3, 2);
    public Setting<Boolean> random = register("Random", true);
    public Setting<Double> offset = register("Offset",  0.5f, 0.1, 2f, 2);

    private ArrayList<PlayerEntity> playersDead = new ArrayList<>();

    @Override
    public void onEnable() {
        playersDead.clear();
    }

    @Subscribe
    private void onTick(TickEvent.Post event){
        if (mc.world == null) {
            playersDead.clear();
            return;
        }

        try {
            if (thunder.get()) mc.world.getPlayers().forEach(this::create);
        }catch (ConcurrentModificationException ignored){}
    }

    public void create(PlayerEntity entity){
        if (playersDead.contains(entity)) {
            if (entity.getHealth() > 0)
                playersDead.remove(entity);
        }
        else {
            if (entity.getHealth() == 0){
                for(int i = 0; i < thunderCount.get(); i++){
                    LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);
                    if (!random.get()) lightningEntity.setPosition(entity.getX(), entity.getY(), entity.getZ());
                    else {
                        Random random1 = new Random();
                        double x = entity.getX() + random1.nextDouble(-offset.get(), offset.get());
                        double y = entity.getY() + random1.nextDouble(-offset.get(), offset.get());
                        double z = entity.getZ() + random1.nextDouble(-offset.get(), offset.get());
                        lightningEntity.setPosition(x, y, z);
                    }
                    mc.world.addEntity(lightningEntity.getId(), lightningEntity);
                }
                playersDead.add(entity);
            }
        }

    }

}
