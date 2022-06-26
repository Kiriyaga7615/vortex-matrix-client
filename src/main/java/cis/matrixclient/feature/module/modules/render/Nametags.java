package cis.matrixclient.feature.module.modules.render;

import cis.matrixclient.event.events.game.GameJoinedEvent;
import cis.matrixclient.event.events.network.PacketEvent;
import cis.matrixclient.event.events.render.EntityLabelRender;
import cis.matrixclient.event.events.render.Render3DEvent;
import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.manager.FriendManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.entity.EntityUtils;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.render.Renderer;
import cis.matrixclient.util.render.Renderer3D;
import com.google.common.eventbus.Subscribe;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Module.Info(name = "Nametags", category = Module.Category.RENDER, drawn = true)
public class Nametags extends Module {
    public enum FormattingMode{
        Italic, Bold, Both, None
    }

    public Page entitiesPage = new Page("Entities");
    public Page colorsPage = new Page("Colors");

    public Setting<Double> scale = register("Scale", 1.0f, 0.1f, 2.0f, 2);
    public Setting<Boolean> health = register("Health", true);
    public Setting<Boolean> ping = register("Ping", true);
    public Setting<Boolean> pops = register("Pops", true);

    public Setting<FormattingMode> formattingMode = register("Formatting", FormattingMode.None, FormattingMode.values());

    public Setting<Boolean> player = register("Player", true, entitiesPage);
    public Setting<Boolean> item = register("Item", true, entitiesPage);
    public Setting<Boolean> mob = register("Mob", false, entitiesPage);
    public Setting<Boolean> animal = register("Animal", false, entitiesPage);


    public Setting<Color> backGroundColor = register("BackgroundColor", new Color(), colorsPage);

    public Setting<Color> playerColor = register("PlayerColor", new Color(), colorsPage);
    public Setting<Color> friendColor = register("FriendColor", new Color(), colorsPage);
    public Setting<Color> itemColor = register("ItemColor", new Color(), colorsPage);
    public Setting<Color> mobColor = register("MobColor", new Color(), colorsPage);
    public Setting<Color> animalColor = register("AnimalColor", new Color(), colorsPage);

    private final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap<>();
    int popped;

    @Subscribe
    private void onLivingLabelRender(EntityLabelRender event){
        if (event.getEntity() instanceof PlayerEntity) event.setCancelled(true);
    }

    @Subscribe
    private void onRender(Render3DEvent event){
        getEntities().forEach(entity -> {
            Vec3d vec3d = entity.getPos().subtract(Renderer3D.getInterpolationOffset(entity)).add(0, entity.getHeight() + 0.25, 0);
            double s = Math.max(scale.get() * (mc.cameraEntity.distanceTo(entity) / 20), 1);

            StringBuilder builder = new StringBuilder(entity.getName().getString());

            if (EntityUtils.isAnimal(entity) || EntityUtils.isMob(entity) || EntityUtils.isPlayer(entity)){
                int health = (int) ((LivingEntity) entity).getHealth();
                Formatting formatting = null;
                switch (getType(health, (int) ((LivingEntity) entity).getMaxHealth())){
                    case Low -> formatting = Formatting.RED;
                    case Middle -> formatting = Formatting.YELLOW;
                    case High -> formatting = Formatting.GREEN;
                }
                builder.append(" ").append(formattingByHealth((LivingEntity) entity));
            }

            if (EntityUtils.isPlayer(entity)){
                builder.append(getAppendInfo((PlayerEntity) entity));
            }

            Text text = Text.of(builder.toString());
            Renderer3D.drawText(text, vec3d.x, vec3d.y, vec3d.z, s, true, getColor(entity).getRgb(), false, backGroundColor.get().getRgb());
        });
    }

    private String getAppendInfo(PlayerEntity player) {
        StringBuilder sb = new StringBuilder();

        sb.append(ping.get() ? " " + getPing(player)+"ms" : "");
        sb.append(pops.get() ? formattingByPops(getPops(player)) : "");

        return sb.toString();
    }

    private int getPops(PlayerEntity p) {
        if (!totemPopMap.containsKey(p.getUuid())) return 0;
        return totemPopMap.getOrDefault(p.getUuid(), 0);
    }

    public int getPing(PlayerEntity player) {
        if (mc.getNetworkHandler() == null) return 0;

        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }


    private String formattingByHealth(LivingEntity player) {
        DecimalFormat df = new DecimalFormat("##");
        double hp = Math.round(player.getHealth() + player.getAbsorptionAmount());
        String health = df.format(Math.round(hp));

        if (hp >= 19) return shortForm() + Formatting.GREEN + " " + health;
        if (hp >= 13 && hp <= 18) return shortForm() + Formatting.YELLOW + " " + health;
        if (hp >= 8 && hp <= 12) return shortForm() + Formatting.GOLD + " " + health;
        if (hp >= 6 && hp <= 7) return  shortForm() + Formatting.RED + " " + health;
        if (hp <= 5) return  shortForm() + Formatting.DARK_RED + " " + health;

        return shortForm() + Formatting.GREEN + " unexpected";
    }

    private String shortForm() {
        return switch (formattingMode.get()) {
            case Italic -> "" + Formatting.ITALIC;
            case Bold -> "" + Formatting.BOLD ;
            case Both -> "" + Formatting.ITALIC + Formatting.BOLD;
            default -> "";
        };
    }

    private String formattingByPops(int pops) {
        switch (pops){
            case 0: return "";
            case 1: return shortForm() + Formatting.GREEN + " -" + pops;
            case 2: return shortForm() + Formatting.DARK_GREEN + " -" + pops;
            case 3: return shortForm() + Formatting.YELLOW + " -" + pops;
            case 4: return shortForm() + Formatting.GOLD + " -" + pops;
            case 5: return shortForm() + Formatting.RED + " -" + pops;
            default: return shortForm() + Formatting.DARK_RED + " -" + pops;

        }
    }

    @Subscribe
    private void onPop(PacketEvent.Receive event) {
        if (!pops.get()) return;
        if (!(event.packet instanceof EntityStatusS2CPacket p)) return;
        if (p.getStatus() != 35) return;
        Entity entity = p.getEntity(mc.world);
        if (!(entity instanceof PlayerEntity)) return;
        if ((entity.equals(mc.player))) return;

        synchronized (totemPopMap) {
            popped = totemPopMap.getOrDefault(entity.getUuid(), 0);
            totemPopMap.put(entity.getUuid(), ++popped);
        }
    }

    @Subscribe
    private void onDeath(TickEvent.Post event) {
        synchronized (totemPopMap) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (!totemPopMap.containsKey(player.getUuid())) continue;

                if (player.deathTime > 0 || player.getHealth() <= 0) totemPopMap.removeInt(player.getUuid());
            }
        }
    }

    @Subscribe
    public void onJoin(GameJoinedEvent event) {
        totemPopMap.clear();
        popped = 0;
    }

    private HealthType getType(int value, int max){
        float off = max / 3;

        if (value <= off) return HealthType.Low;
        if (value > off && value <= 2 * off) return HealthType.Middle;
        if (value > 2 * off) return HealthType.High;

        return HealthType.High;
    }

    public List<Entity> getEntities(){
        List<Entity> entities = new ArrayList<>();
        mc.world.getEntities().forEach(entities::add);

        return entities.stream()
                .filter(e -> (EntityUtils.isPlayer(e) && e != mc.player && player.get())
                        || (e == mc.player && mc.options.getPerspective() != Perspective.FIRST_PERSON)
                        || (e instanceof ItemEntity && item.get())
                        || (EntityUtils.isMob(e) && mob.get())
                        || (EntityUtils.isAnimal(e) && animal.get())).collect(Collectors.toList());
    }


    public Color getColor(Entity e){
        if (EntityUtils.isPlayer(e)) {
            if (FriendManager.isFriend(e.getEntityName())) return friendColor.get();
            return playerColor.get();
        }
        if (e instanceof ItemEntity) return itemColor.get();
        if (EntityUtils.isMob(e)) return mobColor.get();
        if (EntityUtils.isAnimal(e)) return animalColor.get();
        return new Color();
    }

    public enum HealthType{
        Low,
        Middle,
        High
    }
}
