package cis.matrixclient.feature.module.modules.combat.Aura;

import cis.matrixclient.mixins.MinecraftClientAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static cis.matrixclient.MatrixClient.mc;

public class AUtils {
    public static void processAttack(Entity entity) {
        HitResult prevResult = mc.crosshairTarget;

        mc.crosshairTarget = new EntityHitResult(entity, closestVec3d(entity.getBoundingBox()));
        leftClick();
        mc.crosshairTarget = prevResult;
    }

    public static void leftClick() {
        mc.options.attackKey.setPressed(true);
        ((MinecraftClientAccessor) mc).leftClick();
        mc.options.attackKey.setPressed(false);
    }

    public static Vec3d closestVec3d(Box box) {
        if (box == null) return new Vec3d(0.0, 0.0, 0.0);
        Vec3d eyePos = new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        double x = MathHelper.clamp(eyePos.getX(), box.minX, box.maxX);
        double y = MathHelper.clamp(eyePos.getY(), box.minY, box.maxY);
        double z = MathHelper.clamp(eyePos.getZ(), box.minZ, box.maxZ);

        return new Vec3d(x, y, z);
    }
}
