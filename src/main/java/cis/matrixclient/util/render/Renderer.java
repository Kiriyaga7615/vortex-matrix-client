package cis.matrixclient.util.render;

import cis.matrixclient.util.math.ClientMathHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.awt.Color;
import java.nio.FloatBuffer;

import static cis.matrixclient.MatrixClient.mc;

public enum Renderer {
    instance;

    public static void setup2DRender(boolean disableDepth) {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        if (disableDepth)
            RenderSystem.disableDepthTest();
    }

    public static void end2DRender() {
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    public double getScaleFactor() {
        return mc.getWindow().getScaleFactor();
    }

    public int getScaledWidth() {
        return mc.getWindow().getScaledWidth();
    }

    public int getScaledHeight() {
        return mc.getWindow().getScaledHeight();
    }

    public static void drawTexture(MatrixStack matrices, float x, float y, float u, float v, float width, float height,
                                   int textureWidth, int textureHeight) {
        drawTexture(matrices, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    private static void drawTexture(MatrixStack matrices, float x, float y, float width, float height, float u, float v,
                                    float regionWidth, float regionHeight, int textureWidth, int textureHeight) {
        drawTexture(matrices, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth,
                textureHeight);
    }

    private static void drawTexture(MatrixStack matrices, float x0, float y0, float x1, float y1, int z, float regionWidth,
                                    float regionHeight, float u, float v, int textureWidth, int textureHeight) {
        drawTexturedQuad(matrices.peek().getPositionMatrix(), x0, y0, x1, y1, z, (u + 0.0F) / (float) textureWidth,
                (u + regionWidth) / (float) textureWidth, (v + 0.0F) / (float) textureHeight,
                (v + regionHeight) / (float) textureHeight);
    }

    public static void drawTexturedQuad(Matrix4f matrices, float x0, float x1, float y0, float y1, float z, float u0, float u1,
                                        float v0, float v1) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrices, x0, y1, z).texture(u0, v1).next();
        bufferBuilder.vertex(matrices, x1, y1, z).texture(u1, v1).next();
        bufferBuilder.vertex(matrices, x1, y0, z).texture(u1, v0).next();
        bufferBuilder.vertex(matrices, x0, y0, z).texture(u0, v0).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());
    }

    public void fill(MatrixStack matrixStack, float x1, float y1, float x2, float y2, int color) {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        float j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setup2DRender(false);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(g, h, k, f).next();
        BufferRenderer.drawWithShader(bufferBuilder.end());
        end2DRender();
    }

    public java.awt.Color hexToColor(String value) {
        String digits;
        if (value.startsWith("#")) {
            digits = value.substring(1, Math.min(value.length(), 7));
        } else {
            digits = value;
        }
        String hstr = "0x" + digits;
        java.awt.Color c;
        try {
            c = java.awt.Color.decode(hstr);
        } catch (NumberFormatException nfe) {
            c = null;
        }
        return c;
    }

    public static void drawFace(MatrixStack matrixStack, float x, float y, int renderScale, Identifier id) {
        try {
            bindTexture(id);
            drawTexture(matrixStack, x, y, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale,
                    8 * renderScale, 8 * renderScale, 64 * renderScale, 64 * renderScale);
            drawTexture(matrixStack, x, y, 8 * renderScale, 8 * renderScale, 40 * renderScale, 8 * renderScale,
                    8 * renderScale, 8 * renderScale, 64 * renderScale, 64 * renderScale);
        } catch (Exception e) {
        }
    }

    public void fillAndBorder(MatrixStack matrixStack, float left, float top, float right, float bottom, int bcolor,
                              int icolor, float f) {
        fill(matrixStack, left + f, top + f, right - f, bottom - f, icolor);
        fill(matrixStack, left, top, left + f, bottom, bcolor);
        fill(matrixStack, left + f, top, right, top + f, bcolor);
        fill(matrixStack, left + f, bottom - f, right, bottom, bcolor);
        fill(matrixStack, right - f, top + f, right, bottom - f, bcolor);
    }

    public void drawGradientRect(double x, double y, double x2, double y2, int col1, int col2) {
        float f = (float) (col1 >> 24 & 0xFF) / 255F;
        float f1 = (float) (col1 >> 16 & 0xFF) / 255F;
        float f2 = (float) (col1 >> 8 & 0xFF) / 255F;
        float f3 = (float) (col1 & 0xFF) / 255F;

        float f4 = (float) (col2 >> 24 & 0xFF) / 255F;
        float f5 = (float) (col2 >> 16 & 0xFF) / 255F;
        float f6 = (float) (col2 >> 8 & 0xFF) / 255F;
        float f7 = (float) (col2 & 0xFF) / 255F;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        setup2DRender(false);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(x2, y, 0).color(f1, f2, f3, f).next();
        bufferBuilder.vertex(x, y, 0).color(f1, f2, f3, f).next();

        bufferBuilder.vertex(x, y2, 0).color(f5, f6, f7, f4).next();
        bufferBuilder.vertex(x2, y2, 0).color(f5, f6, f7, f4).next();

        BufferRenderer.drawWithShader(bufferBuilder.end());
        end2DRender();
    }

    public void drawFullCircle(int cx, int cy, double r, int c, MatrixStack matrixStack) {
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;

        setup2DRender(false);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; i++) {
            double x = Math.sin(i * 3.141592653589793D / 180.0D) * r;
            double y = Math.cos(i * 3.141592653589793D / 180.0D) * r;
            bufferBuilder.vertex(cx + x, cy + y, -64).color(f1, f2, f3, f).next();
        }
        BufferRenderer.drawWithShader(bufferBuilder.end());
        end2DRender();
    }

    public void drawArc(float cx, float cy, double r, int c, int startpoint, double arc, int linewidth,
                        MatrixStack matrixStack) {
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;

        setup2DRender(false);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        RenderSystem.lineWidth(linewidth);

        for (int i = startpoint; i <= arc; i += 1) {
            double x = Math.sin(i * 3.141592653589793D / 180.0D) * r;
            double y = Math.cos(i * 3.141592653589793D / 180.0D) * r;
            bufferBuilder.vertex(cx + x, cy + y, 0).color(f1, f2, f3, f).next();
        }
        BufferRenderer.drawWithShader(bufferBuilder.end());

        end2DRender();
    }

    public void drawHLine(MatrixStack matrixStack, float par1, float par2, float par3, int par4) {
        if (par2 < par1) {
            float var5 = par1;
            par1 = par2;
            par2 = var5;
        }

        fill(matrixStack, par1, par3, par2 + 1, par3 + 1, par4);
    }

    public void drawVLine(MatrixStack matrixStack, float par1, float par2, float par3, int par4) {
        if (par3 < par2) {
            float var5 = par2;
            par2 = par3;
            par3 = var5;
        }

        fill(matrixStack, par1, par2 + 1, par1 + 1, par3, par4);
    }

    public void glColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public java.awt.Color hex2Rgb(String colorStr) {
        try {
            return new java.awt.Color(Integer.valueOf(colorStr.substring(2, 4), 16),
                    Integer.valueOf(colorStr.substring(4, 6), 16), Integer.valueOf(colorStr.substring(6, 8), 16));
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    public boolean isHovered(float x, float y, float width, float height) {
        return x < getMouseX() && x + width > getMouseX()
                && y < getMouseY() && y + height > getMouseY();
    }

    public boolean hoversCircle(float centerX, float centerY, float radius) {
        Vec2f vec2f = new Vec2f(getMouseX(), getMouseY());
        float distance = ClientMathHelper.INSTANCE.getDistance2D(vec2f, new Vec2f(centerX, centerY));
        return distance <= radius;
    }

    public boolean isOnScreen(Vec3d pos) {
        return pos.getZ() > -1 && pos.getZ() < 1;
    }

    public int getMouseX() {
        return (int) (mc.mouse.getX() * Renderer.instance.getScaledWidth()
                / mc.getWindow().getWidth());
    }

    @SuppressWarnings("resource")
    public int getMouseY() {
        return Renderer.instance.getScaledHeight()
                - (Renderer.instance.getScaledHeight() - (int) mc.mouse.getY()
                * Renderer.instance.getScaledHeight() / mc.getWindow().getHeight()
                - 1);
    }

    public boolean isMouseButtonDown(int button) {
        return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), button) != 0;
    }

    /*
     * public void drawItem(ItemStack stack, float xPosition, float yPosition) {
     * String amountText = stack.getCount() != 1 ? stack.getCount() + "" : "";
     * IItemRenderer iItemRenderer = (IItemRenderer)
     * Wrapper.INSTANCE.getMinecraft().getItemRenderer();
     * iItemRenderer.renderItemIntoGUI(stack, xPosition, yPosition);
     * renderGuiItemOverlay(Wrapper.INSTANCE.getMinecraft().textRenderer, stack,
     * xPosition - 0.5f, yPosition + 1, amountText); }
     */

    @SuppressWarnings("resource")
    public static void drawItem(ItemStack itemStack, int x, int y, double scale, boolean overlay) {
        MatrixStack matrices = RenderSystem.getModelViewStack();

        matrices.push();
        matrices.scale((float) scale, (float) scale, 1);

        mc.getItemRenderer().renderGuiItemIcon(itemStack, (int) (x / scale), (int) (y / scale));
        if (overlay) mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, itemStack, (int) (x / scale), (int) (y / scale), null);

        matrices.pop();
    }

    private static void renderGuiQuad(BufferBuilder buffer, float x, float y, float width, float height, int red, int green,
                                      int blue, int alpha) {
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x + 0, y + 0, 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex(x + 0, y + height, 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + 0, 0.0D).color(red, green, blue, alpha).next();
        Tessellator.getInstance().draw();
    }

    public Vec3d to2D(Vec3d worldPos) {
        Vec3d bound = Renderer3D.get.getRenderPosition(worldPos);
        Vec3d twoD = to2D(bound.x, bound.y, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    private Vec3d to2D(double x, double y, double z) {
        int displayHeight = mc.getWindow().getHeight();
        Vec3f screenCoords = new Vec3f();
        int[] viewport = new int[4];
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer modelView = stack.mallocFloat(16);
            FloatBuffer projection = stack.mallocFloat(16);
            GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelView);
            GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, projection);
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            //snew org.joml.Matrix4f(projection).mul(new org.joml.Matrix4f(modelView)).unproject((float) x, (float) y, (float) z, viewport, screenCoords);
        }
        return new Vec3d(screenCoords.getX() / getScaleFactor(), (displayHeight - screenCoords.getY()) / getScaleFactor(),
                screenCoords.getZ());
    }

    public Vec3d getHeadPos(Entity entity, float partialTicks) {
        Vec3d bound = Renderer3D.get.getEntityRenderPosition(entity, partialTicks);
        Vec3d twoD = to2D(bound.x, bound.y + entity.getHeight() + 0.2, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    public Vec3d getFootPos(Entity entity, float partialTicks) {
        Vec3d bound = Renderer3D.get.getEntityRenderPosition(entity, partialTicks);
        Vec3d twoD = to2D(bound.x, bound.y, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    public Vec3d getPos(Entity entity, float yOffset, float partialTicks) {
        Vec3d bound = Renderer3D.get.getEntityRenderPosition(entity, partialTicks);
        Vec3d twoD = to2D(bound.x, bound.y + yOffset, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    /*
     * public void drawArrow(MatrixStack matrixStack, float x, float y, boolean
     * open, int color) { glColor(color); bindTexture(cog);
     * DrawableHelper.drawTexture(matrixStack, (int) x - 5, (int) y - 5, 0, 0, 10,
     * 10, 10, 10); }
     */

    public static void bindTexture(Identifier identifier) {
        mc.getTextureManager().bindTexture(identifier);
    }

}
