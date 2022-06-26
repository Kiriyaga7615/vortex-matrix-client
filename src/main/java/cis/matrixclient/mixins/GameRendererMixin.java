package cis.matrixclient.mixins;


import cis.matrixclient.MatrixClient;
import cis.matrixclient.event.events.render.Render3DEvent;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.modules.misc.NoMiningTrace;
import cis.matrixclient.feature.module.modules.render.Freecam;
import cis.matrixclient.feature.module.modules.render.NoRender;
import cis.matrixclient.interfaces.IVec3d;
import cis.matrixclient.util.render.Renderer3D;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static cis.matrixclient.MatrixClient.mc;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Camera camera;
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private float floatingItemWidth;
    @Shadow
    private float floatingItemHeight;

    @Shadow
    public abstract void loadProjectionMatrix(Matrix4f matrix4f);

    @Shadow
    protected abstract void bobView(MatrixStack matrixStack, float f);

    @Shadow
    protected abstract void bobViewWhenHurt(MatrixStack matrixStack, float f);

    @Shadow
    protected abstract double getFov(Camera camera, float tickDelta, boolean changingFov);

    @Shadow
    public abstract Matrix4f getBasicProjectionMatrix(double d);

    @Unique
    private Renderer3D renderer;

    @Shadow public abstract void updateTargetedEntity(float tickDelta);

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void renderHand(MatrixStack matrixStack, Camera camera, float tickdelta, CallbackInfo info) {
        if (mc.player == null && mc.world == null) return;

        Render3DEvent event = new Render3DEvent(matrixStack, tickdelta);
        MatrixClient.EVENT_BUS.post(event);
        if (event.isCancelled()) info.cancel();
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float applyCameraTransformationsMathHelperLerpProxy(float delta, float first, float second) {
        if (ModuleManager.getModule(NoRender.class).nausea.get()) return 0;
        return MathHelper.lerp(delta, first, second);
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (floatingItem.getItem() == Items.TOTEM_OF_UNDYING && ModuleManager.getModule(NoRender.class).totem.get()) {
            info.cancel();
        }
    }

    @Inject(method = "bobViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onBobViewWhenHurt(MatrixStack matrixStack, float f, CallbackInfo info) {
        if (ModuleManager.getModule(NoRender.class).hurtcam.get()) info.cancel();
    }

    private boolean freecamSet = false;

    @Inject(method = "updateTargetedEntity", at = @At("HEAD"), cancellable = true)
    private void updateTargetedEntityInvoke(float tickDelta, CallbackInfo info) {
        Freecam freecam = ModuleManager.getModule(Freecam.class);

        if ((freecam.isEnabled()) && client.getCameraEntity() != null && !freecamSet) {
            info.cancel();
            Entity cameraE = client.getCameraEntity();

            double x = cameraE.getX();
            double y = cameraE.getY();
            double z = cameraE.getZ();
            double prevX = cameraE.prevX;
            double prevY = cameraE.prevY;
            double prevZ = cameraE.prevZ;
            float yaw = cameraE.getYaw();
            float pitch = cameraE.getPitch();
            float prevYaw = cameraE.prevYaw;
            float prevPitch = cameraE.prevPitch;


            ((IVec3d) cameraE.getPos()).set(freecam.pos.x, freecam.pos.y - cameraE.getEyeHeight(cameraE.getPose()), freecam.pos.z);
            cameraE.prevX = freecam.prevPos.x;
            cameraE.prevY = freecam.prevPos.y - cameraE.getEyeHeight(cameraE.getPose());
            cameraE.prevZ = freecam.prevPos.z;
            cameraE.setYaw(freecam.yaw);
            cameraE.setPitch(freecam.pitch);
            cameraE.prevYaw = freecam.prevYaw;
            cameraE.prevPitch = freecam.prevPitch;


            freecamSet = true;
            updateTargetedEntity(tickDelta);
            freecamSet = false;

            ((IVec3d) cameraE.getPos()).set(x, y, z);
            cameraE.prevX = prevX;
            cameraE.prevY = prevY;
            cameraE.prevZ = prevZ;
            cameraE.setYaw(yaw);
            cameraE.setPitch(pitch);
            cameraE.prevYaw = prevYaw;
            cameraE.prevPitch = prevPitch;
        }
    }

    @Inject(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"), cancellable = true)
    private void onUpdateTargetedEntity(float tickDelta, CallbackInfo info) {
        if (NoMiningTrace.instance.canWork() && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            client.getProfiler().pop();
            info.cancel();
        }
    }

    @SuppressWarnings("resource")
    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "com/mojang/blaze3d/systems/RenderSystem.clear(IZ)V"))
    private void onRenderWorld(float partialTicks, long finishTimeNano, MatrixStack matrixStack1, CallbackInfo ci) {
        if (mc.getEntityRenderDispatcher().camera == null)
            return;
        RenderSystem.clearColor(1, 1, 1, 1);
        MatrixStack matrixStack = new MatrixStack();
        double d = this.getFov(camera, partialTicks, true);
        matrixStack.peek().getPositionMatrix().multiply(this.getBasicProjectionMatrix(d));
        loadProjectionMatrix(matrixStack.peek().getPositionMatrix());

        this.bobViewWhenHurt(matrixStack, partialTicks);
        Renderer3D.get.applyCameraRots(matrixStack);
        loadProjectionMatrix(matrixStack.peek().getPositionMatrix());
        Render3DEvent.EventRender3DNoBob eventnobob = new Render3DEvent.EventRender3DNoBob(matrixStack, partialTicks);
        MatrixClient.EVENT_BUS.post(eventnobob);
        Renderer3D.get.fixCameraRots(matrixStack);
        if (this.client.options.getBobView().getValue()) {
            bobView(matrixStack, partialTicks);
        }
        Renderer3D.get.applyCameraRots(matrixStack);
        loadProjectionMatrix(matrixStack.peek().getPositionMatrix());
        //EventRenderGetPos eventgetpo EventRenderGetPos(matrixStack, partialTicks).run();
        Renderer3D.get.fixCameraRots(matrixStack);
        loadProjectionMatrix(matrixStack.peek().getPositionMatrix());
    }
}