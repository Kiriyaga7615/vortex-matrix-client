package cis.matrixclient.feature.module.modules.combat.Aura;

import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.manager.FriendManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.combat.Criticals.Criticals;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.math.TimerUtils;
import cis.matrixclient.util.player.Rotations;
import cis.matrixclient.util.player.rotations.Rotation;
import com.google.common.eventbus.Subscribe;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static cis.matrixclient.feature.module.modules.combat.Aura.AUtils.processAttack;

@Module.Info(name = "Aura", category = Module.Category.COMBAT)
public class Aura extends Module {
    public Setting<Weapon> weapon = register("Weapon", Weapon.Any, Weapon.values());
    public Setting<Boolean> smartDelay = register("SmartDelay", true);
    public Setting<Integer> actionDelay = register("ActionDelay", 0, 500, 1000);
    public Setting<Double> attackRange = register("AttackRange", 5, 0, 8, 1);
    public Setting<Boolean> thirtyTwo = register("32k", true);
    public Setting<Boolean> rotate = register("Rotate", false);

    public Setting<Boolean> players = register("Players", true);
    public Setting<Boolean> monsters = register("Monsters", true);
    public Setting<Boolean> animals = register("Animals", true);
    public Setting<Boolean> passive = register("Passive", false);
    public Setting<Boolean> villagers = register("Villagers", false);
    public Setting<Boolean> nametaged = register("Nametaged", false);

    public Entity target;
    private List<Entity> targets = new ArrayList<>();
    private final TimerUtils timer = new TimerUtils();

    @Override
    public void onEnable() {
        target = null;
    }

    @Subscribe
    public void onTick(TickEvent.Post event) {
        targets = getEntities();
        if (targets.isEmpty()) return;

        if (!delayPassed()) return;
        if (!holdsWeapon()) return;

        target = targets.get(0);

        doAttack(target);
        //setDisplayInfo(target.getName().getString());
    }

    private List<Entity> getEntities() {
        targets.clear();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (((LivingEntity) entity).isDead()) continue;

            if (mc.player.distanceTo(entity) <= attackRange.get()) targets.add(entity);
        }

        targets.removeIf(this::badTarget);
        targets.sort(Comparator.comparingDouble(this::distance));
        return targets;
    }

    private double distance(Entity entity) {
        return mc.player.distanceTo(entity);
    }

    private boolean badTarget(Entity entity) {
        if (entity == null) return true;
        if (entity.equals(mc.player) || entity.equals(mc.cameraEntity)) return true;

        if (!passive.get()) {
            if (entity instanceof EndermanEntity enderman && !enderman.isAngry()) return true;
            if (entity instanceof Tameable tameable && tameable.getOwnerUuid() != null && tameable.getOwnerUuid().equals(mc.player.getUuid()))
                return true;
            if (entity instanceof MobEntity mob && !mob.isAttacking() && !(entity instanceof PhantomEntity))
                return true;
        }

        if (entity instanceof PlayerEntity player) {
            if (FriendManager.isFriend(player)) return true;
            if (player.isCreative() || player.isSpectator()) return true;

            return !players.get();
        }
        if (entity instanceof Monster) return !monsters.get();
        if (entity instanceof AnimalEntity) return !animals.get();
        if (entity instanceof VillagerEntity) return !villagers.get();

        return !nametaged.get() && entity.hasCustomName();
    }

    private boolean holdsWeapon() {
        return switch (weapon.get()) {
            case Sword -> mc.player.getMainHandStack().getItem() instanceof SwordItem;
            case Axe -> mc.player.getMainHandStack().getItem() instanceof AxeItem;
            case Both -> mc.player.getMainHandStack().getItem() instanceof SwordItem || mc.player.getMainHandStack().getItem() instanceof AxeItem;
            case Any -> true;
        };
    }

    private boolean delayPassed() {
        if (thirtyTwo.get() && is32k()) return true;
        if (smartDelay.get()) return mc.player.getAttackCooldownProgress(0.5f) >= 1;

        return timer.passedMillis(actionDelay.get());
    }

    private boolean is32k() {
        int level = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, mc.player.getMainHandStack());

        return level >= 20;
    }

    private void doAttack(Entity entity) {
        if (Criticals.instance.isEnabled()) Criticals.instance.doCritical();
        if (rotate.get()) Rotations.rotate(Rotations.getYaw(entity), 0);
        processAttack(entity);

        timer.reset();
    }

    public enum Weapon {
        Sword, Axe, Both, Any
    }

    public static Aura instance;

    public Aura() {
        instance = this;
    }
}
