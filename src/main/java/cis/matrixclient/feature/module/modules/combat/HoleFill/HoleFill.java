package cis.matrixclient.feature.module.modules.combat.HoleFill;

import cis.matrixclient.event.events.world.TickEvent;
import cis.matrixclient.feature.manager.FriendManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.util.BlockPosX;
import cis.matrixclient.util.player.FindItemResult;
import cis.matrixclient.util.player.InvUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;

import static cis.matrixclient.feature.module.modules.combat.HoleFill.HFUtils.place;

@Module.Info(name = "HoleFill", category = Module.Category.COMBAT)
public class HoleFill extends Module {
    public Setting<Integer> blockPerInterval = register("BlocksPerInterval", 2, 1, 5);
    public Setting<Integer> intervalDelay = register("IntervalDelay", 1, 0, 3);
    public Setting<Boolean> rotate = register("Rotate", false);
    public Setting<Double> placeRange = register("PlaceRange", 4.5, 2, 7, 1);
    public Setting<Double> targetRange = register("TargetRange", 7, 5, 12, 1);
    public Setting<Integer> radius = register("Radius", 3, 1, 6); // vertical = 6
    public Setting<Boolean> self = register("Self", true);

    private final ArrayList<BlockPosX> blocks = new ArrayList<>();
    private final ArrayList<PlayerEntity> targets = new ArrayList<>();
    private int interval;

    @Override
    public void onEnable() {
        blocks.clear();
        targets.clear();
        interval = 0;
    }

    @Subscribe
    public void onTick(TickEvent.Pre event) {
        findTargets();
        if (targets.isEmpty()) return;
        blocks.clear();

        for (PlayerEntity target : targets) {
            BlockPosX centerPos = new BlockPosX(target.getBlockPos());

            for (int i = centerPos.getX() - radius.get(); i < centerPos.getX() + radius.get(); i++) {
                for (int j = centerPos.getY() - 6; j < centerPos.getY(); j++) {
                    for (int k = centerPos.getZ() - radius.get(); k < centerPos.getZ() + radius.get(); k++) {
                        BlockPosX pos = new BlockPosX(i, j, k);

                        if (!allowed(pos, target, self.get())) continue;
                        int count = 0;

                        for (Direction direction : Direction.values()) {
                            if (direction == Direction.UP || direction == Direction.DOWN) continue;
                            BlockPosX bpx = pos.offset(direction);

                            if (bpx.isOf(Blocks.BEDROCK) || bpx.isOf(Blocks.OBSIDIAN)) count++;
                        }
                        if (count != 4) continue;

                        if (self.get() && pos.equals(mc.player.getBlockPos())) {
                            blocks.add(pos.up(2));
                        } else blocks.add(pos);
                    }
                }
            }
        }
        //setDisplayInfo(String.valueOf(blocks.size()));
    }

    @Subscribe
    private void onPostTick(TickEvent.Post event) {
        if (targets.isEmpty() || blocks.isEmpty()) return;
        if (interval > 0) interval--;
        if (interval > 0) return;
        FindItemResult block = InvUtils.findInHotbar(Items.OBSIDIAN);

        for (int i = 0; i <= blockPerInterval.get(); i++) {
            if (blocks.size() > i) {
                place(blocks.get(i), rotate.get(), block.slot());
            }
        }
        interval = intervalDelay.get();
    }

    private boolean allowed(BlockPosX pos, PlayerEntity target, boolean self) {
        if (!pos.isAir()) return false;
        if (pos.distance() > placeRange.get()) return false;
        if (pos.equals(target.getBlockPos())) return false;
        if (pos.equals(mc.player.getBlockPos()) && !self) return false;
        for (int h = 0; h < 4; h++) {
            if (!pos.up(h).isAir()) return false;
        }
        return !pos.down().isAir();
    }

    private void findTargets() {
        targets.clear();
        for (PlayerEntity e : mc.world.getPlayers()) {
            if (e.isCreative() || e == mc.player) continue;

            if (!e.isDead() && e.isAlive() && !FriendManager.isFriend(e) && e.distanceTo(mc.player) <= targetRange.get()) {
                targets.add(e);
            }
        }
    }
}