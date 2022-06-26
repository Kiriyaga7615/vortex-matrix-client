package cis.matrixclient.feature.module.modules.render;

import cis.matrixclient.event.events.render.Render3DEvent;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.entity.EntityUtils;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.render.Renderer3D;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Module.Info(name = "ESP", category = Module.Category.RENDER, drawn = true)
public class ESP extends Module {
    public Page entitiesPage = new Page("Entities");

    public Page playerPage = new Page("Player");
    public Page itemPage = new Page("Item");
    public Page crystalPage = new Page("Crystal");
    public Page mobPage = new Page("Mob");
    public Page animalPage = new Page("Animal");

    public Setting<Boolean> player = register("Player", true, entitiesPage);
    public Setting<Boolean> item = register("Item", true, entitiesPage);
    public Setting<Boolean> crystal = register("Crystal", false, entitiesPage);
    public Setting<Boolean> mob = register("Mob", false, entitiesPage);
    public Setting<Boolean> animal = register("Animal", false, entitiesPage);

    public Setting<Color> playerColor = register("PlayerColor", new Color(), playerPage);
    public Setting<Color> itemColor = register("ItemColor", new Color(), itemPage);
    public Setting<Color> crystalColor = register("CrystalColor", new Color(), crystalPage);
    public Setting<Color> mobColor = register("MobColor", new Color(), mobPage);
    public Setting<Color> animalColor = register("AnimalColor", new Color(), animalPage);


    private HashMap<Integer, Box> renderBox = new HashMap<>();

    @Subscribe
    private void onRender(Render3DEvent event){
        getEntities().forEach(entity -> {

            Box post = entity.getBoundingBox();
            renderBox.putIfAbsent(entity.getId(), post);
            double minxX = (post.minX - renderBox.get(entity.getId()).minX) / 2;
            double minxY = (post.minY - renderBox.get(entity.getId()).minY) / 2;
            double minxZ = (post.minZ - renderBox.get(entity.getId()).minZ) / 2;

            double maxX = (post.maxX - renderBox.get(entity.getId()).maxX) / 2;
            double maxY = (post.maxY - renderBox.get(entity.getId()).maxY) / 2;
            double maxZ = (post.maxZ - renderBox.get(entity.getId()).maxZ) / 2;

            renderBox.replace(entity.getId(), new Box(renderBox.get(entity.getId()).minX + minxX, renderBox.get(entity.getId()).minY + minxY, renderBox.get(entity.getId()).minZ + minxZ, renderBox.get(entity.getId()).maxX + maxX, renderBox.get(entity.getId()).maxY + maxY,  renderBox.get(entity.getId()).maxZ + maxZ));

            Vec3d vec3d = renderBox.get(entity.getId()).getCenter();
            Vec3d fixedVec = Renderer3D.getRenderPosition(vec3d);
            Box box = new Box(fixedVec.x - 0.5, fixedVec.y - 0.5, fixedVec.z - 0.5, fixedVec.x + 0.5, fixedVec.y + 0.5, fixedVec.z + 0.5);


            Renderer3D.get.drawEntityBox(event.getMatrixStack(), entity, event.getPartialTicks(), getColor(entity));
        });
    }

    public List<Entity> getEntities(){
        List<Entity> entities = new ArrayList<>();
        mc.world.getEntities().forEach(entities::add);

        return entities.stream()
                .filter(e -> (EntityUtils.isPlayer(e) && e != mc.player && player.get())
                        || (e == mc.player && mc.options.getPerspective() != Perspective.FIRST_PERSON)
                        || (e instanceof ItemEntity && item.get())
                        || (e instanceof EndCrystalEntity && crystal.get())
                        || (EntityUtils.isMob(e) && mob.get())
                        || (EntityUtils.isAnimal(e) && animal.get())).collect(Collectors.toList());
    }

    public int getColor(Entity e){
        if (EntityUtils.isPlayer(e)) return playerColor.get().getRgb();
        if (e instanceof ItemEntity) return itemColor.get().getRgb();
        if (e instanceof EndCrystalEntity) return crystalColor.get().getRgb();
        if (EntityUtils.isMob(e)) return mobColor.get().getRgb();
        if (EntityUtils.isAnimal(e)) return animalColor.get().getRgb();
        return -1;
    }
}
