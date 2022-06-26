package cis.matrixclient.util.render;

import cis.matrixclient.feature.module.modules.client.HUD;
import cis.matrixclient.util.render.font.FontRenderers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.awt.Color;
import java.util.ArrayList;

import static cis.matrixclient.MatrixClient.mc;

@SuppressWarnings("resource")
public enum Renderer3D {
    get;

    public static void drawText(Text text, double x, double y, double z, double scale, boolean shadow, int color, boolean custom, int background) {
        drawText(text, x, y, z, 0, 0, scale, shadow, color, custom, background);
    }

    /** Draws text in the world. **/
    public static void drawText(Text text, double x, double y, double z, double offX, double offY, double scale, boolean fill, int color, boolean custom, int background) {
        MatrixStack matrices = matrixFrom(x, y, z);

        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        matrices.translate(offX, offY, 0);
        matrices.scale(-0.025f * (float) scale, -0.025f * (float) scale, 1);

        int halfWidth = custom ? (int) FontRenderers.getCustomNormal(18).getStringWidth(text.getString()) / 2 : mc.textRenderer.getWidth(text) / 2;

        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

        if (fill) {
            int opacity = (int) (MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;
            if (custom) FontRenderers.getCustomNormal(18).drawString(matrices, text.getString(), -halfWidth, 0f, color);
            else mc.textRenderer.draw(text, -halfWidth, 0f, color, false, matrices.peek().getPositionMatrix(), immediate, true, background, 0xf000f0);
            immediate.draw();
        } else {
            matrices.push();
            matrices.translate(1, 1, 0);
            if (custom) FontRenderers.getCustomNormal(18).drawString(matrices, text.getString(), -halfWidth, 0f, color);
            else mc.textRenderer.draw(text.copy(), -halfWidth, 0f, color, false, matrices.peek().getPositionMatrix(), immediate, true, 0, 0xf000f0);
            immediate.draw();
            matrices.pop();
        }

        if (custom) FontRenderers.getCustomNormal(18).drawString(matrices, text.getString(), -halfWidth, 0f, color);
        else mc.textRenderer.draw(text, -halfWidth, 0f, color, false, matrices.peek().getPositionMatrix(), immediate, true, 0, 0xf000f0);
        immediate.draw();

        RenderSystem.disableBlend();
    }


    public void fancyUrn(MatrixStack matrixStack, Box box, int color) {
        drawLine(matrixStack, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, color);
        drawLine(matrixStack, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, color);
        drawLine(matrixStack, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ, color);
        drawLine(matrixStack, box.maxX, box.maxY, box.minZ, box.minX, box.maxY, box.minZ, color);

        drawLine(matrixStack, getPartPlus(box.minX, box.maxX, 5), box.minY, getPartPlus(box.minZ, box.maxZ, 5), getPartPlus(box.minX, box.maxX, 5), box.minY, getPartMinus(box.minZ, box.maxZ, 5), color);
        drawLine(matrixStack, getPartPlus(box.minX, box.maxX, 5), box.minY, getPartMinus(box.minZ, box.maxZ, 5), getPartMinus(box.minX, box.maxX, 5), box.minY, getPartMinus(box.minZ, box.maxZ, 5), color);
        drawLine(matrixStack, getPartMinus(box.minX, box.maxX, 5), box.minY, getPartMinus(box.minZ, box.maxZ, 5), getPartMinus(box.minX, box.maxX, 5), box.minY, getPartPlus(box.minZ, box.maxZ, 5), color);
        drawLine(matrixStack, getPartMinus(box.minX, box.maxX, 5), box.minY, getPartPlus(box.minZ, box.maxZ, 5), getPartPlus(box.minX, box.maxX, 5), box.minY, getPartPlus(box.minZ, box.maxZ, 5), color);

        drawLine(matrixStack, box.minX, box.maxY, box.minZ, getPartPlus(box.minX, box.maxX, 5), box.minY, getPartPlus(box.minZ, box.maxZ, 5), color);
        drawLine(matrixStack, box.minX, box.maxY, box.maxZ, getPartPlus(box.minX, box.maxX, 5), box.minY, getPartMinus(box.minZ, box.maxZ, 5), color);
        drawLine(matrixStack, box.maxX, box.maxY, box.maxZ, getPartMinus(box.minX, box.maxX, 5), box.minY, getPartMinus(box.minZ, box.maxZ, 5), color);
        drawLine(matrixStack, box.maxX, box.maxY, box.minZ, getPartMinus(box.minX, box.maxX, 5), box.minY, getPartPlus(box.minZ, box.maxZ, 5), color);
    }

    public void fancyBox(MatrixStack matrixStack, Box box, int color) {

        ArrayList<Sexx> sexxes = new ArrayList<>();

        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 4), box.minY, box.minZ, box.minX, getPartPlus(box.minY, box.maxY, 4), box.minZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 4), box.minY, box.minZ, getPartPlus(box.minX, box.maxX, 4), box.minY, box.minZ));
        sexxes.add(new Sexx(box.minX, getPartPlus(box.minY, box.maxY, 4), box.minZ, getPartPlus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.minZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.minZ, getPartPlus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.minZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.minZ, getPartPlus(box.minX, box.maxX, 4), box.maxY, box.minZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 4), box.maxY, box.minZ, getPartMinus(box.minX, box.maxX, 4), box.maxY, box.minZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.minZ, getPartMinus(box.minX, box.maxX, 4), box.maxY, box.minZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.minZ, box.maxX, getPartMinus(box.minY, box.maxY, 4), box.minZ));
        sexxes.add(new Sexx(box.maxX, getPartMinus(box.minY, box.maxY, 4), box.minZ, box.maxX, getPartPlus(box.minY, box.maxY, 4), box.minZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), getPartPlus(box.minY, box.maxY, 6), box.minZ, box.maxX, getPartPlus(box.minY, box.maxY, 4), box.minZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), getPartPlus(box.minY, box.maxY, 6), box.minZ, getPartMinus(box.minX, box.maxX, 4), box.minY, box.minZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 4), box.minY, box.minZ, getPartPlus(box.minX, box.maxX, 4), box.minY, box.minZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), getPartPlus(box.minY, box.maxY, 6), box.maxZ, box.minX, getPartPlus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), getPartPlus(box.minY, box.maxY, 6), box.maxZ, getPartPlus(box.minX, box.maxX, 4), box.minY, box.maxZ));
        sexxes.add(new Sexx(box.minX, getPartPlus(box.minY, box.maxY, 4), box.maxZ, box.minX, getPartMinus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.maxZ, box.minX, getPartMinus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.maxZ, getPartPlus(box.minX, box.maxX, 4), box.maxY, box.maxZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 4), box.maxY, box.maxZ, getPartMinus(box.minX, box.maxX, 4), box.maxY, box.maxZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.maxZ, getPartMinus(box.minX, box.maxX, 4), box.maxY, box.maxZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), getPartMinus(box.minY, box.maxY, 6), box.maxZ, box.maxX, getPartMinus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(box.maxX, getPartMinus(box.minY, box.maxY, 4), box.maxZ, box.maxX, getPartPlus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), getPartPlus(box.minY, box.maxY, 6), box.maxZ, box.maxX, getPartPlus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), getPartPlus(box.minY, box.maxY, 6), box.maxZ, getPartMinus(box.minX, box.maxX, 4), box.minY, box.maxZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 4), box.minY, box.maxZ, getPartPlus(box.minX, box.maxX, 4), box.minY, box.maxZ));
        sexxes.add(new Sexx(box.minX, getPartPlus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6), box.minX, getPartPlus(box.minY, box.maxY, 4), box.minZ));
        sexxes.add(new Sexx(box.minX, getPartPlus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6), box.minX, box.minY, getPartPlus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(box.minX, getPartPlus(box.minY, box.maxY, 4), box.minZ, box.minX, getPartMinus(box.minY, box.maxY, 4), box.minZ));
        sexxes.add(new Sexx(box.minX, getPartMinus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6), box.minX, getPartMinus(box.minY, box.maxY, 4), box.minZ));
        sexxes.add(new Sexx(box.minX, getPartMinus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6), box.minX, box.maxY, getPartPlus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(box.minX, box.maxY, getPartPlus(box.minZ, box.maxZ, 4), box.minX, box.maxY, getPartMinus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(box.minX, getPartMinus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6), box.minX, box.maxY, getPartMinus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(box.minX, getPartMinus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6), box.minX, getPartMinus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(box.minX, getPartMinus(box.minY, box.maxY, 4), box.maxZ, box.minX, getPartPlus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(box.minX, getPartPlus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6), box.minX, getPartPlus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(box.minX, getPartPlus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6), box.minX, box.minY, getPartMinus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(box.minX, box.minY, getPartMinus(box.minZ, box.maxZ, 4), box.minX, box.minY, getPartPlus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(box.maxX, getPartPlus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6), box.maxX, getPartPlus(box.minY, box.maxY, 4), box.minZ));
        sexxes.add(new Sexx(box.maxX, getPartPlus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6), box.maxX, box.minY, getPartPlus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(box.maxX, getPartPlus(box.minY, box.maxY, 4), box.minZ, box.maxX, getPartMinus(box.minY, box.maxY, 4), box.minZ));
        sexxes.add(new Sexx(box.maxX, getPartMinus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6), box.maxX, getPartMinus(box.minY, box.maxY, 4), box.minZ));
        sexxes.add(new Sexx(box.maxX, getPartMinus(box.minY, box.maxY, 6), getPartPlus(box.minZ, box.maxZ, 6), box.maxX, box.maxY, getPartPlus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(box.maxX, box.maxY, getPartPlus(box.minZ, box.maxZ, 4), box.maxX, box.maxY, getPartMinus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(box.maxX, getPartMinus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6), box.maxX, box.maxY, getPartMinus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(box.maxX, getPartMinus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6), box.maxX, getPartMinus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(box.maxX, getPartMinus(box.minY, box.maxY, 4), box.maxZ, box.maxX, getPartPlus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(box.maxX, getPartPlus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6), box.maxX, getPartPlus(box.minY, box.maxY, 4), box.maxZ));
        sexxes.add(new Sexx(box.maxX, getPartPlus(box.minY, box.maxY, 6), getPartMinus(box.minZ, box.maxZ, 6), box.maxX, box.minY, getPartMinus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(box.maxX, box.minY, getPartMinus(box.minZ, box.maxZ, 4), box.maxX, box.minY, getPartPlus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), box.maxY, getPartPlus(box.minZ, box.maxZ, 6), getPartPlus(box.minX, box.maxX, 4), box.maxY, box.minZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), box.maxY, getPartPlus(box.minZ, box.maxZ, 6), box.minX, box.maxY, getPartPlus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), box.maxY, getPartMinus(box.minZ, box.maxZ, 6), box.minX, box.maxY, getPartMinus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), box.maxY, getPartMinus(box.minZ, box.maxZ, 6), getPartPlus(box.minX, box.maxX, 4), box.maxY, box.maxZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), box.maxY, getPartMinus(box.minZ, box.maxZ, 6), getPartMinus(box.minX, box.maxX, 4), box.maxY, box.maxZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), box.maxY, getPartMinus(box.minZ, box.maxZ, 6), box.maxX, box.maxY, getPartMinus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), box.maxY, getPartPlus(box.minZ, box.maxZ, 6), box.maxX, box.maxY, getPartPlus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), box.maxY, getPartPlus(box.minZ, box.maxZ, 6), getPartMinus(box.minX, box.maxX, 4), box.maxY, box.minZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), box.minY, getPartPlus(box.minZ, box.maxZ, 6), box.minX, box.minY, getPartPlus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), box.minY, getPartPlus(box.minZ, box.maxZ, 6), getPartPlus(box.minX, box.maxX, 4), box.minY, box.minZ));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), box.minY, getPartMinus(box.minZ, box.maxZ, 6), box.minX, box.minY, getPartMinus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(getPartPlus(box.minX, box.maxX, 6), box.minY, getPartMinus(box.minZ, box.maxZ, 6), getPartPlus(box.minX, box.maxX, 4), box.minY, box.maxZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), box.minY, getPartMinus(box.minZ, box.maxZ, 6), getPartMinus(box.minX, box.maxX, 4), box.minY, box.maxZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), box.minY, getPartMinus(box.minZ, box.maxZ, 6), box.maxX, box.minY, getPartMinus(box.minZ, box.maxZ, 4)));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), box.minY, getPartPlus(box.minZ, box.maxZ, 6), getPartMinus(box.minX, box.maxX, 4), box.minY, box.minZ));
        sexxes.add(new Sexx(getPartMinus(box.minX, box.maxX, 6), box.minY, getPartPlus(box.minZ, box.maxZ, 6), box.maxX, box.minY, getPartPlus(box.minZ, box.maxZ, 4)));

        drawOptimizedLine(matrixStack, color, sexxes);
    }

    public void fancyBox(MatrixStack matrixstack, Entity entity, float partialTicks, int color) {
        Vec3d renderPos = getEntityRenderPosition(entity, partialTicks);
        fancyBox(matrixstack, entity, renderPos.x, renderPos.y, renderPos.z, color);
    }

    public void fancyBox(MatrixStack matrixStack, Entity entity, double x, double y, double z, int color) {
        setup3DRender(true);
        matrixStack.translate(x, y, z);
        matrixStack.multiply(new Quaternion(new Vec3f(0, -1, 0), entity instanceof ItemEntity ? 90 : entity.getYaw(), true));
        matrixStack.translate(-x, -y, -z);

        Box box = new Box(x - entity.getWidth() + entity.getWidth() / 4, y, z - entity.getWidth() + entity.getWidth() / 4, x + entity.getWidth() - entity.getWidth() / 4, y + entity.getHeight() + 0.1, z + entity.getWidth() - entity.getWidth() / 4);
        if (entity instanceof ItemEntity)
            box = new Box(x - 0.22, y + 0.05, z - 0.22, x + 0.22, y + 0.49, z + 0.22);

        RenderSystem.lineWidth(90);

        fancyBox(matrixStack, box, color);

        end3DRender();
        matrixStack.translate(x, y, z);
        matrixStack.multiply(new Quaternion(new Vec3f(0, 1, 0), entity instanceof ItemEntity ? 90 : entity.getYaw(), true));
        matrixStack.translate(-x, -y, -z);
    }

    private static double getPartPlus(double min, double max, int i) {
        return min + (max - min) / i;
    }

    private static double getPartPlus(double min, double max, int i, int q) {
        return min + ((max - min) / i) * q;
    }

    private static double getPartMinus(double min, double max, int i) {
        return max - (max - min) / i;
    }

    private static double getPartMinus(double min, double max, int i, int q) {
        return max - ((max - min) / i) * q;
    }


    public static Vec3d getEntityRenderPosition(Entity entity, double partial) {
        double x = entity.prevX + ((entity.getX() - entity.prevX) * partial) - mc.getEntityRenderDispatcher().camera.getPos().x;
        double y = entity.prevY + ((entity.getY() - entity.prevY) * partial) - mc.getEntityRenderDispatcher().camera.getPos().y;
        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * partial) - mc.getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(x, y, z);
    }

    public static Vec3d getRenderPosition(double x, double y, double z) {
        double minX = x - mc.getEntityRenderDispatcher().camera.getPos().x;
        double minY = y - mc.getEntityRenderDispatcher().camera.getPos().y;
        double minZ = z - mc.getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public static Vec3d getRenderPosition(Vec3d vec3d) {
        double minX = vec3d.getX() - mc.getEntityRenderDispatcher().camera.getPos().x;
        double minY = vec3d.getY() - mc.getEntityRenderDispatcher().camera.getPos().y;
        double minZ = vec3d.getZ() - mc.getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public static Vec3d getRenderPosition(BlockPos blockPos) {
        double minX = blockPos.getX() - mc.getEntityRenderDispatcher().camera.getPos().x;
        double minY = blockPos.getY() - mc.getEntityRenderDispatcher().camera.getPos().y;
        double minZ = blockPos.getZ() - mc.getEntityRenderDispatcher().camera.getPos().z;
        return new Vec3d(minX, minY, minZ);
    }

    public void fixCameraRots(MatrixStack matrixStack) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        matrixStack.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));
        matrixStack.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(camera.getPitch()));
    }

    public void applyCameraRots(MatrixStack matrixStack) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));
    }

    public void setup3DRender(boolean disableDepth) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (disableDepth)
            RenderSystem.disableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        RenderSystem.enableCull();
    }

    public void end3DRender() {
        RenderSystem.enableTexture();
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }


    public void drawSphere(MatrixStack matrixStack, float radius, int gradation, int color, boolean testDepth, Vec3d pos) {
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        java.awt.Color color1 = new java.awt.Color(color, true);
        final float PI = 3.141592f;
        float x, y, z, alpha, beta;
        if (!testDepth)
            RenderSystem.disableDepthTest();
        RenderSystem.disableTexture();
        for (alpha = 0.0f; alpha < Math.PI; alpha += PI / gradation) {
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (beta = 0.0f; beta < 2.01f * Math.PI; beta += PI / gradation) {
                x = (float) (pos.getX() + (radius * Math.cos(beta) * Math.sin(alpha)));
                y = (float) (pos.getY() + (radius * Math.sin(beta) * Math.sin(alpha)));
                z = (float) (pos.getZ() + (radius * Math.cos(alpha)));
                Vec3d renderPos = getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float) renderPos.x, (float) renderPos.y, (float) renderPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                x = (float) (pos.getX() + (radius * Math.cos(beta) * Math.sin(alpha + PI / gradation)));
                y = (float) (pos.getY() + (radius * Math.sin(beta) * Math.sin(alpha + PI / gradation)));
                z = (float) (pos.getZ() + (radius * Math.cos(alpha + PI / gradation)));
                renderPos = getRenderPosition(x, y, z);
                bufferBuilder.vertex(matrix4f, (float) renderPos.x, (float) renderPos.y, (float) renderPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
            }
            BufferRenderer.drawWithShader(bufferBuilder.end());
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
    }

    public void drawBoxWithDepthTest(MatrixStack matrixstack, Box bb, int color) {
        setup3DRender(false);

        drawFilledBox(matrixstack, bb, color & 0x70ffffff);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixstack, bb, color);

        end3DRender();
    }

    public void drawBoxWithOutline(MatrixStack matrixStack, BlockPos blockPos, int color) {
        if (blockPos == null) return;

        Vec3d vec3d = getRenderPosition(blockPos);
        Box box = new Box(vec3d.x, vec3d.y, vec3d.z, vec3d.x + 1, vec3d.y + 1, vec3d.z + 1);
        drawBox(matrixStack, box, color);
        drawOutlineBox(matrixStack, box, color);
    }

    public void drawBoxWithOutline(MatrixStack matrixStack, Box box) {
        if (box == null) return;

        drawBox(matrixStack, box, HUD.get.color());
        drawOutlineBox(matrixStack, box, HUD.get.color());
    }


    public void drawBox(MatrixStack matrixstack, Box bb, int color) {
        setup3DRender(true);

        drawFilledBox(matrixstack, bb, color & 0x70ffffff);
        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixstack, bb, color);

        end3DRender();
    }

    public void drawBoxOutline(MatrixStack matrixstack, Box bb, int color) {
        setup3DRender(true);

        RenderSystem.lineWidth(1);
        drawOutlineBox(matrixstack, bb, color);

        end3DRender();
    }

    public void drawBoxInside(MatrixStack matrixstack, Box bb, int color) {
        setup3DRender(true);

        drawFilledBox(matrixstack, bb, color & 0x70ffffff);

        end3DRender();
    }

    public void drawEntityBox(MatrixStack matrixstack, Entity entity, float partialTicks, int color) {
        Vec3d renderPos = getEntityRenderPosition(entity, partialTicks);
        drawEntityBox(matrixstack, entity, renderPos.x, renderPos.y, renderPos.z, color);
    }

    public void drawEntityBox(MatrixStack matrixstack, Entity entity, double x, double y, double z, int color) {
        setup3DRender(true);
        matrixstack.translate(x, y, z);
        matrixstack.multiply(new Quaternion(new Vec3f(0, -1, 0), 90, true));
        matrixstack.translate(-x, -y, -z);

        Box bb = new Box(x - entity.getWidth() + entity.getWidth() / 4, y, z - entity.getWidth() + entity.getWidth() / 4, x + entity.getWidth() - entity.getWidth() / 4, y + entity.getHeight() + 0.1, z + entity.getWidth() - entity.getWidth() / 4);
        if (entity instanceof ItemEntity)
            bb = new Box(x - 0.22, y + 0.05, z - 0.22, x + 0.22, y + 0.49, z + 0.22);

        RenderSystem.lineWidth(90);
        drawOutlineBox(matrixstack, bb, color);

        end3DRender();
        matrixstack.translate(x, y, z);
        matrixstack.multiply(new Quaternion(new Vec3f(0, 1, 0), 90, true));
        matrixstack.translate(-x, -y, -z);
    }

    public double interpolate(final double now, final double then, final double percent) {
        return (then + (now - then) * percent);
    }

    public void drawFilledBox(MatrixStack matrixStack, Box bb, int color) {
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        java.awt.Color color1 = new java.awt.Color(color, true);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS/*QUADS*/, VertexFormats.POSITION_COLOR);
        float minX = (float) bb.minX;
        float minY = (float) bb.minY;
        float minZ = (float) bb.minZ;
        float maxX = (float) bb.maxX;
        float maxY = (float) bb.maxY;
        float maxZ = (float) bb.maxZ;

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, maxX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, maxX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        bufferBuilder.vertex(matrix4f, minX, minY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, minY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, maxZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, minX, maxY, minZ).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());
    }

    public static void drawLine(MatrixStack matrixStack, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        java.awt.Color color1 = new java.awt.Color(color, true);
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES/*LINES*/, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x1, y1, z1).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, x2, y2, z2).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        ;

        BufferRenderer.drawWithShader(bufferBuilder.end());
    }

    public static void drawLine(MatrixStack matrixStack, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        java.awt.Color color1 = new java.awt.Color(color, true);
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, (float) z1).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, (float) z2).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        BufferRenderer.drawWithShader(bufferBuilder.end());
    }

    public static void drawOptimizedLine(MatrixStack matrixStack, int color, ArrayList<Sexx> sexxes) {
        java.awt.Color color1 = new java.awt.Color(color, true);
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (Sexx sexx : sexxes) {
            bufferBuilder.vertex(matrix4f, (float) sexx.x1, (float) sexx.y1, (float) sexx.z1).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
            bufferBuilder.vertex(matrix4f, (float) sexx.x2, (float) sexx.y2, (float) sexx.z2).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();

        }
        BufferRenderer.drawWithShader(bufferBuilder.end());
    }

    public void drawOutlineBox(MatrixStack matrixStack, Box bb, int color) {
        java.awt.Color color1 = new Color(color, true);
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES/*LINES*/, VertexFormats.POSITION_COLOR);

        VoxelShape shape = VoxelShapes.cuboid(bb);
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, (float) z1).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
            bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, (float) z2).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
        });

        BufferRenderer.drawWithShader(bufferBuilder.end());
    }

    // A Pointer to RenderSystem.shaderLightDirections
    private static final Vec3f[] shaderLight;

    static {
        try {
            shaderLight = (Vec3f[]) FieldUtils.getField(RenderSystem.class, "shaderLightDirections", true).get(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Draws text in the world.
     **/
    public static void drawText(Text text, double x, double y, double z, double scale, boolean fill) {
        drawText(text, x, y, z, 0, 0, scale, fill);
    }

    /**
     * Draws text in the world.
     **/

    public static void drawText(Text text, double x, double y, double z, double offX, double offY, double scale, boolean fill) {
        MatrixStack matrices = matrixFrom(x, y, z);

        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        matrices.translate(offX, offY, 0);
        matrices.scale(-0.025f * (float) scale, -0.025f * (float) scale, 1);

        int halfWidth = mc.textRenderer.getWidth(text) / 2;

        VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

        if (fill) {
            int opacity = (int) (mc.options.getTextBackgroundOpacity(0.6F) * 255.0F) << 24;
            mc.textRenderer.draw(text, -halfWidth, 0f, 553648127, false, matrices.peek().getPositionMatrix(), immediate, true, opacity, 0xf000f0);
            immediate.draw();
        } else {
            matrices.push();
            matrices.translate(1, 1, 0);
            immediate.draw();
            matrices.pop();
        }

        mc.textRenderer.draw(text, -halfWidth, 0f, -1, false, matrices.peek().getPositionMatrix(), immediate, true, 0, 0xf000f0);
        immediate.draw();

        RenderSystem.disableBlend();
    }

    /**
     * Draws a 2D gui items somewhere in the world.
     **/

    public static void drawItem(ItemStack itemStack, int x, int y, double scale, boolean overlay) {
        if (overlay)
            mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, itemStack, (int) (x / scale), (int) (y / scale), null);
        mc.getItemRenderer().renderGuiItemIcon(itemStack, (int) (x / scale), (int) (y / scale));
    }

    public static void drawGuiItem(double x, double y, double z, double offX, double offY, double scale, ItemStack item) {
        if (item.isEmpty()) {
            return;
        }

        MatrixStack matrices = matrixFrom(x, y, z);

        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));

        matrices.translate(offX, offY, 0);
        matrices.scale((float) scale, (float) scale, 0.001f);

        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180f));

        mc.getBufferBuilders().getEntityVertexConsumers().draw();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Vec3f[] currentLight = shaderLight.clone();
        DiffuseLighting.disableGuiDepthLighting();

        mc.getItemRenderer().renderItem(item, ModelTransformation.Mode.GUI, 0xF000F0,
                OverlayTexture.DEFAULT_UV, matrices, mc.getBufferBuilders().getEntityVertexConsumers(), 0);

        mc.getBufferBuilders().getEntityVertexConsumers().draw();

        RenderSystem.setShaderLights(currentLight[0], currentLight[1]);
        RenderSystem.disableBlend();
    }

    public static MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();

        Camera camera = mc.gameRenderer.getCamera();
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));

        matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

        return matrices;
    }

    public static Vec3d getInterpolationOffset(Entity e) {
        if (mc.isPaused()) {
            return Vec3d.ZERO;
        }

        double tickDelta = mc.getTickDelta();
        return new Vec3d(
                e.getX() - MathHelper.lerp(tickDelta, e.lastRenderX, e.getX()),
                e.getY() - MathHelper.lerp(tickDelta, e.lastRenderY, e.getY()),
                e.getZ() - MathHelper.lerp(tickDelta, e.lastRenderZ, e.getZ()));
    }

    public static class Sexx {
        double x1;
        double y1;
        double z1;
        double x2;
        double y2;
        double z2;


        public Sexx(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;

        }
    }
}
