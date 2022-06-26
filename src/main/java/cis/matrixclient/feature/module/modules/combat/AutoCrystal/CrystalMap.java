package cis.matrixclient.feature.module.modules.combat.AutoCrystal;

public class CrystalMap {
    private int crystalId;
    public int attacks;
    private int tick;

    public boolean shouldWait;

    public CrystalMap(int crystalId, int attacks) {
        this.crystalId = crystalId;
        this.attacks = attacks;
        this.shouldWait = false;
        this.tick = 0;
    }

    public int getId() {
        return crystalId;
    }

    public void tick() {
        if (shouldWait) tick++;
    }

    public boolean canHit() {
        return tick > AutoCrystal.instance.passedTicks.get();
    }
}
