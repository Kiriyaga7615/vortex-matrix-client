package cis.matrixclient.feature.module.modules.combat.AutoCrystal;

import cis.matrixclient.event.events.network.PacketEvent;
import cis.matrixclient.event.events.render.Render3DEvent;
import cis.matrixclient.event.events.world.BlockUpdateEvent;
import cis.matrixclient.event.events.world.EntityAddedEvent;
import cis.matrixclient.event.events.world.EntityRemovedEvent;
import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.manager.FriendManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.module.modules.combat.PistonCrystal.PistonCrystal;
import cis.matrixclient.feature.setting.Page;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.mixins.ClientPlayerInteractionManagerAccessor;
import cis.matrixclient.util.math.TimerUtils;
import cis.matrixclient.util.player.DamageUtils;
import cis.matrixclient.util.player.FindItemResult;
import cis.matrixclient.util.player.InvUtils;
import cis.matrixclient.util.player.Rotations;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.render.Renderer3D;
import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cis.matrixclient.feature.module.modules.combat.AutoCrystal.ACUtils.*;

/**
 * @author Eureka
 */

@Module.Info(name = "AutoCrystal", category = Module.Category.COMBAT)
public class AutoCrystal extends Module {
    public Page placeAndBreak = new Page("Place And Break");
    public Page damages = new Page("Damages");
    public Page predictions = new Page("Prediction");
    public Page misc = new Page("Misc");
    public Page pause = new Page("Pause");
    public Page renderPage = new Page("Render");

    // General
    public Setting<Swap> swap = register("Swap", Swap.Normal, Swap.values());
    public Setting<Integer> swapDelay = register("SwapDelay", 0, 0, 20);
    public Setting<Boolean> syncSlot = register("SyncSlot", true);

    // Place and Break
    public Setting<Integer> placeDelay = register("PlaceDelay", 0, 0, 10, placeAndBreak);
    public Setting<Double> placeRange = register("PlaceRange", 4.7, 0, 7, 1, placeAndBreak);
    public Setting<Integer> breakDelay = register("BreakDelay", 0, 0, 10, placeAndBreak);
    public Setting<Double> breakRange = register("BreakRange", 4.7, 0, 7, 1, placeAndBreak);
    public Setting<Boolean> smartRange = register("SmartRange", true, placeAndBreak);
    public Setting<FastBreak> fastBreak = register("FastBreak", FastBreak.OFF, FastBreak.values(), placeAndBreak);
    public Setting<Frequency> freqMode = register("FreqMode", Frequency.Divide, Frequency.values(), placeAndBreak);
    public Setting<Integer> frequency = register("Frequency", 20, 0, 20, placeAndBreak);
    public Setting<Integer> ticksExisted = register("TicksExisted", 1, 0, 5, placeAndBreak);
    public Setting<Boolean> multiPlace = register("MultiPlace", false, placeAndBreak);
    public Setting<Boolean> oneTwelve = register("1.12", false, placeAndBreak);
    public Setting<Boolean> rotate = register("Rotate", false, placeAndBreak);
    public Setting<Boolean> rayTrace = register("RayTrace", false, placeAndBreak);
    public Setting<Boolean> ignoreTerrain = register("IgnoreTerrain", true, placeAndBreak);
    public Setting<Integer> blockUpdate = register("BlockUpdate", 200, 0, 500, placeAndBreak);
    public Setting<Boolean> limit = register("Limit", true, placeAndBreak);
    public Setting<Integer> limitAttacks = register("LimitAttacks", 5, 1, 10, placeAndBreak);
    public Setting<Integer> passedTicks = register("PassedTicks", 10, 0, 20, placeAndBreak);
    public Setting<Priority> priority = register("Prio", Priority.Break, Priority.values(), placeAndBreak);

    // Damage
    public Setting<Boolean> predict = register("Predict", false, predictions);
    public Setting<Double> predictOffset = register("PredictValue", 0.50, 0, 3, 2, predictions);
    public Setting<Boolean> collision = register("Collision", true, predictions);
    public Setting<Boolean> predictID = register("PredictID", false, predictions);
    public Setting<Integer> delayID = register("DelayID", 0, 0, 5, predictions);

    public Setting<Place> doPlace = register("Place", Place.BestDMG, Place.values(), damages);
    public Setting<Break> doBreak = register("Break", Break.BestDMG, Break.values(), damages);
    public Setting<Double> minDmg = register("MinDamage", 7.5, 0, 36, 1, damages);
    public Setting<Double> safety = register("Safety", 25, 0, 100, 1, damages);
    public Setting<Boolean> antiSelfPop = register("AntiSelfPop", true, damages);
    public Setting<Boolean> antiFriendDamage = register("AntiFriendDamage", false, damages);
    public Setting<Double> friendMaxDmg = register("MaxDamage", 8, 0, 36, 1, damages);

    // Misc
    public Setting<Double> faceBreaker = register("FaceBreaker", 11, 0, 36, 1, misc);
    public Setting<Double> armorBreaker = register("ArmorBreaker", 25, 0, 100, 1, misc);
    public Setting<Boolean> support = register("Support", false, misc);
    public Setting<Integer> supportDelay = register("SupportDelay", 5, 0, 10, misc);
    public Setting<Boolean> crystalOnBreak = register("CrystalOnBreak", false, misc);
    public Setting<SurroundBreak> surroundBreak = register("SurroundBreak", SurroundBreak.OnMine, SurroundBreak.values(), misc);

    // Pause
    public Setting<Boolean> eatPause = register("EatPause", true, pause);
    public Setting<Boolean> minePause = register("MinePause", false, pause);

    // Render
    public Setting<Render> render = register("Render", Render.Smooth, Render.values(), renderPage);
    public Setting<Color> color = register("Color", new Color(), renderPage);
    public Setting<Integer> smoothFactor = register("SmoothFactor", 10, 5, 20, renderPage);
    public Setting<Integer> renderTime = register("RenderTime", 10, 0, 15, renderPage);
    public Setting<Boolean> renderPredict = register("RenderPredict", true, renderPage);

    private final ExecutorService thread = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * (1 + 13 / 3));

    private BlockPos bestPos = new BlockPos(4, 2, 0);
    private double bestDamage = 0;

    public int placeTimer, breakTimer, swapTimer, idTimer;
    public int attacks, ticksPassed;
    public boolean firstTime;

    private boolean shouldSupport;

    public BlockPos renderPos;
    public Vec3d predictPos;
    public int renderTimer;

    private int lastEntityId, last;
    private Hand hand;

    private BlockPos updatedBlock;

    public IntSet brokenCrystals = new IntOpenHashSet();
    public final int[] second = new int[20];
    public static int cps;
    public int tick, i, lastSpawned = 20;

    private final List<CrystalMap> crystalMap = new ArrayList<>();
    private final TimerUtils blockTimer = new TimerUtils();
    private final TimerUtils supportTimer = new TimerUtils();

    @Override
    public void onEnable() {
        placeTimer = 0;
        swapTimer = 0;
        idTimer = 0;
        supportTimer.reset();

        bestPos = null;
        updatedBlock = null;
        renderPos = null;

        shouldSupport = false;
        renderTimer = renderTime.get();
        brokenCrystals.clear();
        firstTime = true;

        tick = 0;
        Arrays.fill(second, 0);
        i = 0;

        crystalMap.clear();
        blockTimer.reset();
        //setDisplayInfo("0.0, 0");
    }

    @Subscribe
    public void onTick(TickEvent.Post event) {
        getCPS();
        updateBlock();

        if (renderTimer > 0 && renderPos != null) renderTimer--;
        if (placeTimer > 0) placeTimer--;
        if (breakTimer > 0) breakTimer--;
        if (swapTimer > 0) swapTimer--;
        if (idTimer > 0) idTimer--;

        if (ticksPassed > 0) ticksPassed--;
        else {
            ticksPassed = 20;
            attacks = 0;
        }

        if (eatPause.get() && mc.player.isUsingItem() && (mc.player.getMainHandStack().isFood() || mc.player.getOffHandStack().isFood()))
            return;
        if (minePause.get() && mc.interactionManager.isBreakingBlock()) return;
        if (PistonCrystal.instance.shouldPause()) return;

        thread.execute(this::doCrystalOnBreak);
        thread.execute(this::doSurroundBreak);
        thread.execute(this::doSupport);
        thread.execute(this::doCalculate);

        if (firstTime && priority.get(Priority.Smart)) {
            doPlace();
            doBreak();
            doPlace();
            firstTime = !firstTime;
        } else if (priority.get(Priority.Place)) {
            doPlace();
            doBreak();
        } else {
            doBreak();
            doPlace();
        }

        crystalMap.forEach(CrystalMap::tick);
    }

    @Subscribe
    public void onAdded(EntityAddedEvent event) {
        if (!(event.entity instanceof EndCrystalEntity)) return;

        if ((fastBreak.get(FastBreak.Instant) || fastBreak.get(FastBreak.All))) {
            if (bestPos == null) return;

            if (bestPos.equals(event.entity.getBlockPos().down())) {
                doBreak(event.entity, false);
            }
        }

        last = event.entity.getId() - lastEntityId;
        lastEntityId = event.entity.getId();
    }

    @Subscribe
    public void onRemove(EntityRemovedEvent event) {
        if (!(event.entity instanceof EndCrystalEntity)) return;

        if (brokenCrystals.contains(event.entity.getId())) {
            lastSpawned = 20;
            tick++;

            removeId(event.entity);
        }
    }

    @Subscribe
    private void onSend(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            swapTimer = swapDelay.get();
        }
    }

    @Subscribe
    private void onReceive(PacketEvent.Receive event) {
        if (!(event.packet instanceof PlaySoundIdS2CPacket packet)) return;

        if (fastBreak.get(FastBreak.Sequential) || fastBreak.get(FastBreak.All)) {
            if (packet.getCategory().getName().equals(SoundCategory.BLOCKS.getName()) && packet.getSoundId().getPath().equals("entity.generic.explode")) {
                brokenCrystals.forEach(crystalMap -> mc.world.removeEntity(crystalMap, Entity.RemovalReason.KILLED));
            }
        }
    }

    @Subscribe
    public void onBlockUpdate(BlockUpdateEvent event) {
        if (event.newState.isAir()) {
            updatedBlock = event.pos;
            blockTimer.reset();
        }
    }

    private void doPlace() {
        doPlace(bestPos);
        //setDisplayInfo(getBestDamage() + ", " + AutoCrystal.cps);
    }

    private void doPlace(BlockPos blockPos) {
        if (blockPos == null) bestDamage = 0;
        if (blockPos == null || placeTimer > 0) return;

        FindItemResult crystal = InvUtils.findInHotbar(Items.END_CRYSTAL);
        if (!crystal.found()) return;

        hand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (distanceTo(closestVec3d(new Box(blockPos))) > placeRange.get()) return;
        BlockHitResult hitResult = getResult(blockPos);

        if (!swap.get(Swap.OFF) && !crystal.isOffhand() && !crystal.isMainHand()) InvUtils.swap(crystal);

        if (crystal.isOffhand() || crystal.isMainHand()) {
            if (rotate.get()) Rotations.rotate(Rotations.getYaw(blockPos), Rotations.getPitch(blockPos));

            if (!hasEntity(new Box(blockPos.up()))) mc.player.swingHand(hand);
            else mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult, 0));
        }

        if (predictID.get() && idTimer <= 0) {
            EndCrystalEntity endCrystal = new EndCrystalEntity(mc.world, blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5);
            endCrystal.setShowBottom(false);
            endCrystal.setId(lastEntityId + last);

            doBreak(endCrystal, false);
            endCrystal.kill();

            idTimer = delayID.get();
        }

        if (swap.get(Swap.Silent)) {
            InvUtils.swapBack();
            if (syncSlot.get()) InvUtils.syncSlots();
        }

        placeTimer = placeDelay.get();
        setRender(blockPos);
    }

    private void doBreak() {
        doBreak(getCrystal(), true);
    }

    private void doBreak(Entity entity, boolean checkAge) {
        if (entity == null || breakTimer > 0 || swapTimer > 0 || !frequency() || (checkAge && entity.age < ticksExisted.get()))
            return;

        if (limit.get()) {
            if (!getCrystal(entity).canHit() && getCrystal(entity).attacks > limitAttacks.get()) {
                getCrystal(entity).shouldWait = true;
            }
        }

        if (distanceTo(closestVec3d(entity.getBoundingBox())) > breakRange.get()) return;
        processAttack(entity, Attack.Vanilla);

        if (!matchesCrystal(entity)) crystalMap.add(new CrystalMap(entity.getId(), 1));
        else getCrystal(entity).attacks++;

        if (fastBreak.get(FastBreak.Kill)) {
            entity.kill();

            lastSpawned = 20;
            tick++;
        }

        addBroken(entity);
        attacks++;
        breakTimer = breakDelay.get();
    }

    private BlockHitResult getResult(BlockPos blockPos) {
        Vec3d eyesPos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        if (!rayTrace.get()) return new BlockHitResult(closestVec3d(new Box(blockPos)), Direction.UP, blockPos, false);
        for (Direction direction : Direction.values()) {
            RaycastContext raycastContext = new RaycastContext(eyesPos, new Vec3d(blockPos.getX() + 0.5 + direction.getVector().getX() * 0.5,
                    blockPos.getY() + 0.5 + direction.getVector().getY() * 0.5,
                    blockPos.getZ() + 0.5 + direction.getVector().getZ() * 0.5), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
            BlockHitResult result = mc.world.raycast(raycastContext);
            if (result != null && result.getType() == HitResult.Type.BLOCK && result.getBlockPos().equals(blockPos)) {
                return result;
            }
        }

        return new BlockHitResult(closestVec3d(new Box(blockPos)), blockPos.getY() == 255 ? Direction.DOWN : Direction.UP, blockPos, false);
    }

    private boolean matchesCrystal(Entity entity) {
        return getCrystal(entity).attacks != 0;
    }

    private CrystalMap getCrystal(Entity entity) {
        for (CrystalMap crystal : crystalMap) {
            if (crystal.getId() == entity.getId()) return crystal;
        }

        return new CrystalMap(-9999, 0);
    }

    private void doCrystalOnBreak() {
        if (!crystalOnBreak.get()) return;

        try {
            float progress = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getBreakingProgress();
            BlockPos breakingPos = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getCurrentBreakingBlockPos();
            if (!partOfSurround(breakingPos)) return;

            if (progress > 0.96F) doPlace(breakingPos);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private void doSurroundBreak() {
        if (surroundBreak.get(SurroundBreak.OFF)) return;
        if (isFacePlacing()) return;

        if (surroundBreak.get(SurroundBreak.OnMine) && !mc.interactionManager.isBreakingBlock()) return;
        List<BlockPos> vulnerablePos = new ArrayList<>();

        try {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) continue;
                if (FriendManager.isFriend(player)) continue;
                if (!isSurrounded(player)) continue;

                for (BlockPos bp : getSphere(player, 5)) {
                    if (hasEntity(new Box(bp), entity -> entity == mc.player || entity == player || entity instanceof ItemEntity))
                        continue;

                    boolean canPlace = mc.world.isAir(bp.up()) &&
                            (mc.world.getBlockState(bp).isOf(Blocks.OBSIDIAN) || mc.world.getBlockState(bp).isOf(Blocks.BEDROCK));

                    if (!canPlace) continue;
                    Vec3d vec3d = new Vec3d(bp.getX(), bp.getY() + 1, bp.getZ());
                    Box endCrystal = new Box(vec3d.x - 0.5, vec3d.y, vec3d.z - 0.5, vec3d.x + 1.5, vec3d.y + 2, vec3d.z + 1.5);

                    for (BlockPos surround : getSurroundBlocks(player, true)) {
                        if (mc.world.getBlockState(surround).getHardness(mc.world, surround) <= 0) return;

                        if (surroundBreak.get(SurroundBreak.OnMine) && mc.player.getMainHandStack().getItem() instanceof PickaxeItem) {
                            BlockPos breakingPos = ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).getCurrentBreakingBlockPos();
                            if (breakingPos == null) return;

                            if (!surround.equals(breakingPos)) continue;
                        }
                        Box box = new Box(surround);

                        if (endCrystal.intersects(box)) vulnerablePos.add(bp);
                    }
                }
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }


        if (vulnerablePos.isEmpty()) return;
        vulnerablePos.sort(Comparator.comparingDouble(ACUtils::distanceTo));
        BlockPos blockPos = vulnerablePos.get(0);

        if (hasEntity(new Box(blockPos.up())) || distanceTo(closestVec3d(new Box(blockPos))) > placeRange.get()) return;
        doPlace(blockPos);
    }

    private void doCalculate() {
        FindItemResult crystal = InvUtils.findInHotbar(Items.END_CRYSTAL);
        if (!crystal.found()) return;

        List<BlockPos> sphere = getSphere(mc.player, Math.ceil(placeRange.get()));
        BlockPos bestPos = null;
        double bestDamage = 0.0;
        double safety = 0.0;

        try {
            for (BlockPos bp : sphere) {
                if (distanceTo(closestVec3d(new Box(bp))) > placeRange.get()) continue;

                boolean canPlace = mc.world.isAir(bp.up()) &&
                        (mc.world.getBlockState(bp).isOf(Blocks.OBSIDIAN) || mc.world.getBlockState(bp).isOf(Blocks.BEDROCK));

                if (!canPlace) continue;
                if (updatedBlock != null && updatedBlock.equals(bp.up())) continue;

                EndCrystalEntity fakeCrystal = new EndCrystalEntity(mc.world, bp.getX() + 0.5, bp.getY() + 1.0, bp.getZ() + 0.5);

                if (smartRange.get() && distanceTo(closestVec3d(fakeCrystal.getBoundingBox())) > breakRange.get())
                    continue;
                if (oneTwelve.get() && !mc.world.isAir(bp.up(2))) continue;

                double targetDamage = getHighestDamage(roundVec(bp), null);
                double selfDamage = DamageUtils.crystalDamage(mc.player, roundVec(bp));
                safety = (targetDamage / 36 - selfDamage / 36) * 100;

                if (safety < this.safety.get()
                        || antiSelfPop.get() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount())
                    continue;

                boolean validPos = true;
                if (antiFriendDamage.get()) {
                    for (PlayerEntity friend : mc.world.getPlayers()) {
                        if (!FriendManager.isFriend(friend)) continue;

                        double friendDamage = DamageUtils.crystalDamage(friend, roundVec(bp));
                        if (friendDamage > friendMaxDmg.get()) {
                            validPos = false;
                            break;
                        }
                    }
                }
                if (!validPos) continue;
                if (intersectsWithEntity(bp, multiPlace.get(), fakeCrystal)) continue;


                if (targetDamage > bestDamage) {
                    bestDamage = targetDamage;
                    bestPos = bp;
                }
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }

        this.bestPos = bestPos;
        this.bestDamage = bestDamage;
    }

    private void doSupport() {
        if (!support.get() || !supportTimer.passedTicks(supportDelay.get())) return;
        List<BlockPos> support = getSphere(mc.player, Math.ceil(placeRange.get()));
        List<BlockPos> placePositions = support.stream().filter(this::canPlace).toList();
        if (placePositions.isEmpty()) shouldSupport = true;

        FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);
        if (!shouldSupport || bestPos != null || !obsidian.found()) return;
        BlockPos bestPos = null;

        double bestDamage = 0.0;
        double safety = 0.0;

        try {
            for (BlockPos bp : support) {
                if (distanceTo(closestVec3d(new Box(bp))) > placeRange.get()) continue;

                boolean canPlace = mc.world.getBlockState(bp).isAir() && mc.world.getBlockState(bp.up()).isAir();
                if (!canPlace) continue;
                if (hasEntity(new Box(bp.up()))) continue;

                double targetDamage = getHighestDamage(roundVec(bp), bp);
                double selfDamage = DamageUtils.crystalDamage(mc.player, roundVec(bp));

                safety = (targetDamage / 36 - selfDamage / 36) * 100;

                if (safety < this.safety.get()
                        || antiSelfPop.get() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount())
                    continue;

                if (targetDamage > bestDamage) {
                    bestDamage = targetDamage;
                    bestPos = bp;
                }
            }

            if (bestPos != null) {
                Hand hand = obsidian.isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND;
                BlockHitResult hitResult = new BlockHitResult(closestVec3d(new Box(bestPos)), Direction.DOWN, bestPos, false);

                InvUtils.swap(obsidian);
                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult, 0));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
                InvUtils.swapBack();
                InvUtils.syncSlots();
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }

        shouldSupport = false;
        supportTimer.reset();
    }

    private boolean canPlace(BlockPos blockPos) {
        if (!mc.world.getBlockState(blockPos.up()).getFluidState().isEmpty()) return false;
        if (!(mc.world.isAir(blockPos.up()) && (mc.world.getBlockState(blockPos).isOf(Blocks.OBSIDIAN) || mc.world.getBlockState(blockPos).isOf(Blocks.BEDROCK))))
            return false;

        return !hasEntity(new Box(blockPos.up()), entity -> !(entity instanceof EndCrystalEntity));
    }

    private boolean intersectsWithEntity(BlockPos blockPos, boolean multiPlace, EndCrystalEntity fakeCrystal) {
        if (multiPlace) {
            return hasEntity(new Box(blockPos).stretch(0, 2, 0));
        } else {
            return hasEntity(new Box(blockPos).stretch(0, 2, 0), entity -> !(entity instanceof EndCrystalEntity && entity.getPos().getX() == fakeCrystal.getPos().getX() && entity.getPos().getY() == fakeCrystal.getPos().getY() && entity.getPos().getZ() == fakeCrystal.getPos().getZ()));
        }
    }

    private void updateBlock() {
        if (updatedBlock != null && blockTimer.passedMillis(blockUpdate.get())) {
            updatedBlock = null;
        }
    }

    private double getHighestDamage(Vec3d vec3d, @Nullable BlockPos supportPos) {
        if (mc.world == null || mc.player == null) return 0;
        if (mc.world.getPlayers().isEmpty()) return 0;

        double highestDamage = 0;

        for (PlayerEntity target : mc.world.getPlayers()) {
            if (FriendManager.isFriend(target)) continue;
            if (target == mc.player) continue;
            if (target.isDead() || target.getHealth() == 0) continue;

            double targetDamage = 0;
            boolean skipPredict = false;
            if (predict.get()) {
                if (!isSurrounded(target)) {
                    OtherClientPlayerEntity fakeTarget = new OtherClientPlayerEntity(mc.world, new GameProfile(target.getUuid(), "FakeTarget"), target.getPublicKey());
                    fakeTarget.setHealth(target.getHealth());
                    fakeTarget.setAbsorptionAmount(target.getAbsorptionAmount());
                    fakeTarget.getInventory().clone(target.getInventory());
                    fakeTarget.setVelocity(0, 0, 0);

                    double x = getPredict(target, predictOffset.get())[0];
                    double z = getPredict(target, predictOffset.get())[1];

                    fakeTarget.setPosition(x, target.getY(), z);

                    if (collision.get()) {
                        Iterable<VoxelShape> collisions = mc.world.getBlockCollisions(fakeTarget, fakeTarget.getBoundingBox());

                        if (collisions.iterator().hasNext()) skipPredict = true;
                    }
                    targetDamage = skipPredict ? 0 : DamageUtils.crystalDamage(fakeTarget, vec3d, supportPos, ignoreTerrain.get());
                    if (!skipPredict) predictPos = fakeTarget.getBoundingBox().getCenter();
                }
            }
            if (!predict.get() || skipPredict) {
                targetDamage = DamageUtils.crystalDamage(target, vec3d, supportPos, ignoreTerrain.get());
                predictPos = target.getBoundingBox().getCenter();
            }

            if (targetDamage < minDmg.get() && !shouldFacePlace(target, targetDamage)) continue;

            if (doPlace.get(Place.BestDMG)) {
                if (targetDamage > highestDamage) {
                    highestDamage = targetDamage;
                }
            } else highestDamage += targetDamage;
        }

        return highestDamage;
    }

    private double[] getPredict(PlayerEntity player, double offset) {
        double x = player.getX(), z = player.getZ();
        double predictX = 0, predictZ = 0;

        if (player.getX() > player.prevX) {
            predictX = player.getX() - player.prevX;
            x += predictX + offset;
        } else if (player.getX() < player.prevX) {
            predictX = player.prevX - player.getX();
            x -= predictX + offset;
        }

        if (player.getZ() > player.prevZ) {
            predictZ = player.getZ() - player.prevZ;
            z += predictZ + offset;
        } else if (player.getZ() < player.prevZ) {
            predictZ = player.prevZ - player.getZ();
            z -= predictZ + offset;
        }

        return new double[]{x, z};
    }

    private EndCrystalEntity getCrystal() {
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;
            if (mc.player.distanceTo(entity) > breakRange.get()) continue;

            if (getCrystal(entity).shouldWait) continue;
            if (doBreak.get(Break.All)) return (EndCrystalEntity) entity;

            double tempDamage = getHighestDamage(roundVec(entity), null);
            if (tempDamage > minDmg.get()) return (EndCrystalEntity) entity;
        }

        if (bestPos == null) return null;
        return getEntity(bestPos.up());
    }

    private double getBestDamage() {
        return ((double) Math.round(bestDamage * 100) / 100);
    }

    private boolean frequency() {
        switch (freqMode.get()) {
            case EachTick -> {
                if (attacks > frequency.get()) return false;
            }
            case Divide -> {
                if (!divide(frequency.get()).contains(ticksPassed)) return false;
            }
            case OFF -> {
                return true;
            }
        }

        return true;
    }

    public void getCPS() {
        i++;
        if (i >= second.length) i = 0;

        second[i] = tick;
        tick = 0;

        cps = 0;
        for (int i : second) cps += i;

        lastSpawned--;
        if (lastSpawned >= 0 && cps > 0) cps--;
        if (cps == 0) bestDamage = 0.0;
    }

    public ArrayList<Integer> divide(int frequency) {
        ArrayList<Integer> freqAttacks = new ArrayList<>();
        int size = 0;

        if (20 < frequency) return freqAttacks;
        else if (20 % frequency == 0) {
            for (int i = 0; i < frequency; i++) {
                size += 20 / frequency;
                freqAttacks.add(size);
            }
        } else {
            int zp = frequency - (20 % frequency);
            int pp = 20 / frequency;

            for (int i = 0; i < frequency; i++) {
                if (i >= zp) {
                    size += pp + 1;
                    freqAttacks.add(size);
                } else {
                    size += pp;
                    freqAttacks.add(size);
                }
            }
        }

        return freqAttacks;
    }

    private void addBroken(Entity entity) {
        if (!brokenCrystals.contains(entity.getId())) brokenCrystals.add(entity.getId());
    }

    private void removeId(Entity entity) {
        if (brokenCrystals.contains(entity.getId())) brokenCrystals.remove(entity.getId());
    }

    private boolean shouldFacePlace(PlayerEntity player, double damage) {
        if (faceBreaker.get() == 0 || damage < 1.5) return false;
        if (!isSurrounded(player) || isFaceTrapped()) return false;
        return (player.getHealth() + player.getAbsorptionAmount()) <= faceBreaker.get() || getWorstArmor(player) <= armorBreaker.get();
    }

    private boolean isFacePlacing() {
        return bestPos != null && partOfSurround(bestPos);
    }

    private boolean partOfSurround(BlockPos blockPos) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            if (getSurroundBlocks(player, true).contains(blockPos)) return true;
        }

        return false;
    }

    public void setRender(BlockPos blockPos) {
        renderPos = blockPos;
        renderTimer = renderTime.get();
    }

    Box renderBox = null;

    @Subscribe
    public void onRender(Render3DEvent event) {
        if (bestPos == null) return;
        if (render.get(Render.OFF)) return;
        if (renderTimer == 0) renderPos = null;
        if (renderPos == null) return;

        Box post = new Box(renderPos);
        if (renderBox == null) renderBox = post;

        double x = (post.minX - renderBox.minX) / smoothFactor.get();
        double y = (post.minY - renderBox.minY) / smoothFactor.get();
        double z = (post.minZ - renderBox.minZ) / smoothFactor.get();

        renderBox = new Box(renderBox.minX + x, renderBox.minY + y, renderBox.minZ + z, renderBox.maxX + x, renderBox.maxY + y, renderBox.maxZ + z);

        Vec3d vec3d = render.get(Render.Box) ? new Vec3d(renderPos.getX(), renderPos.getY(), renderPos.getZ()) : renderBox.getCenter(); // Для рендера дамага

        Vec3d fixedVec = Renderer3D.getRenderPosition(vec3d);
        Box box = new Box(fixedVec.x - 0.5, fixedVec.y - 0.5, fixedVec.z - 0.5, fixedVec.x + 0.5, fixedVec.y + 0.5, fixedVec.z + 0.5);

        if (render.get(Render.Box))
            box = new Box(fixedVec.x, fixedVec.y, fixedVec.z, fixedVec.x + 1, fixedVec.y + 1, fixedVec.z + 1);

        Renderer3D.get.drawBox(event.getMatrixStack(), box, color.get().getRgb());

        // Predict
        if (!predict.get() || !renderPredict.get() || predictPos == null) return;

        Vec3d predictVec = Renderer3D.getRenderPosition(predictPos);
        Box predictBox = new Box(predictVec.x - 0.2, predictVec.y - 0.2, predictVec.z - 0.2, predictVec.x + 0.2, predictVec.y + 0.2, predictVec.z + 0.2);
        Renderer3D.get.drawBox(event.getMatrixStack(), predictBox, color.get().getRgb());
    }


    @Override
    public String getInfo() {
        return getBestDamage() + ", " + AutoCrystal.cps;
    }

    public enum Swap {
        Silent, Normal, OFF
    }

    public enum FastBreak {
        Kill, Instant, Sequential, All, OFF
    }

    public enum Attack {
        Packet, Client, Vanilla
    }

    public enum Frequency {
        EachTick, Divide, OFF
    }

    public enum Priority {
        Place, Break, Smart
    }

    public enum Place {
        BestDMG, MostDMG
    }

    public enum Break {
        All, PlacePos, BestDMG
    }

    public enum SurroundBreak {
        Always, OnMine, OFF
    }

    public enum Render {
        Box, Smooth, OFF
    }

    public static AutoCrystal instance;

    public AutoCrystal() {
        instance = this;
    }
}