package cis.matrixclient.util.entity;

import cis.matrixclient.feature.manager.FriendManager;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.client.FakePlayer;
import cis.matrixclient.feature.module.modules.combat.AntiBots;
import cis.matrixclient.util.entity.otherplayerentity.FakeOtherClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static cis.matrixclient.MatrixClient.mc;

public class TargetUtils {
    private static AntiBots antiBots = ModuleManager.getModule(AntiBots.class);
    private static FakePlayer fakePlayer = ModuleManager.getModule(FakePlayer.class);

    public static List<PlayerEntity> getTargetsInRange(double enemyRange) {
        List<PlayerEntity> stream = mc.world.getPlayers()
                .stream()
                .filter(e -> e != mc.player)
                .filter(e -> e.isAlive())
                .filter(e -> !FriendManager.isFriend(e.getEntityName()))
                .filter(e -> !(e instanceof FakeOtherClientPlayerEntity))
                .filter(e -> ((PlayerEntity) e).getHealth() > 0)
                .filter(e -> mc.player.distanceTo(e) < enemyRange)
                .filter(e -> !ModuleManager.getModule(AntiBots.class).isEnabled() || !(e == fakePlayer.fakePlayer))
                .sorted(Comparator.comparing(e -> mc.player.distanceTo(e)))
                .collect(Collectors.toList());

        return stream;
    }

    public static List<Entity> getEntitiesInRange(double range){
        List<Entity> entities = new ArrayList<>();
        mc.world.getEntities().forEach(entities::add);

        return entities.stream()
                .filter(e -> e != mc.player)
                .filter(e -> e.isAlive())
                .filter(e -> mc.player.distanceTo(e) < range)
                .filter(e -> !(e instanceof FakeOtherClientPlayerEntity))
                .filter(e -> !ModuleManager.getModule(AntiBots.class).isEnabled() || !(e == fakePlayer.fakePlayer))
                .sorted(Comparator.comparing(e -> mc.player.distanceTo(e)))
                .collect(Collectors.toList());
    }


    private static final List<Entity> ENTITIES = new ArrayList<>();

    public static Entity get(Predicate<Entity> isGood, SortPriority sortPriority) {
        ENTITIES.clear();
        getList(ENTITIES, isGood, sortPriority, 1);
        if (!ENTITIES.isEmpty()) {
            return ENTITIES.get(0);
        }

        return null;
    }

    public static void getList(List<Entity> targetList, Predicate<Entity> isGood, SortPriority sortPriority, int maxCount) {
        targetList.clear();

        for (Entity entity : mc.world.getEntities()) {
            if (entity != null && isGood.test(entity)) targetList.add(entity);
        }

        targetList.sort((e1, e2) -> sort(e1, e2, sortPriority));
        targetList.removeIf(entity -> targetList.indexOf(entity) > maxCount -1);
    }

    public static PlayerEntity getPlayerTarget(double range, SortPriority priority) {
        if (mc.world == null) return null;
        return (PlayerEntity) get(entity -> {
            if (!(entity instanceof PlayerEntity) || entity == mc.player) return false;
            if (((PlayerEntity) entity).isDead() || ((PlayerEntity) entity).getHealth() <= 0) return false;
            if (mc.player.distanceTo(entity) > range) return false;
            if (FriendManager.isFriend(entity.getName().getString())) return false;
            if (antiBots.enabled && entity == fakePlayer.fakePlayer) return false;
            return !((PlayerEntity) entity).isCreative() || entity instanceof OtherClientPlayerEntity;
        }, priority);
    }

    public static boolean isBadTarget(PlayerEntity target, double range) {
        if (target == null) return true;
        return mc.player.distanceTo(target) > range || !target.isAlive() || target.isDead() || target.getHealth() <= 0 || (antiBots.enabled && target == fakePlayer.fakePlayer);
    }

    private static int sort(Entity e1, Entity e2, SortPriority priority) {
        return switch (priority) {
            case LowestDistance -> Double.compare(e1.distanceTo(mc.player), e2.distanceTo(mc.player));
            case HighestDistance -> invertSort(Double.compare(e1.distanceTo(mc.player), e2.distanceTo(mc.player)));
            case LowestHealth -> sortHealth(e1, e2);
            case HighestHealth -> invertSort(sortHealth(e1, e2));
        };
    }

    private static int sortHealth(Entity e1, Entity e2) {
        boolean e1l = e1 instanceof LivingEntity;
        boolean e2l = e2 instanceof LivingEntity;

        if (!e1l && !e2l) return 0;
        else if (e1l && !e2l) return 1;
        else if (!e1l) return -1;

        return Float.compare(((LivingEntity) e1).getHealth(), ((LivingEntity) e2).getHealth());
    }
    private static int invertSort(int sort) {
        if (sort == 0) return 0;
        return sort > 0 ? -1 : 1;
    }

    public enum SortPriority {
        LowestDistance, HighestDistance, LowestHealth, HighestHealth
    }
}
