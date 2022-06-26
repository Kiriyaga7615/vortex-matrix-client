package cis.matrixclient.feature.module.modules.client;

import cis.matrixclient.event.events.game.GameJoinedEvent;
import cis.matrixclient.event.events.network.PacketEvent;
import cis.matrixclient.event.events.render.Render2DEvent;
import cis.matrixclient.feature.manager.ModuleManager;
import cis.matrixclient.feature.module.Module;
import cis.matrixclient.feature.setting.Setting;
import cis.matrixclient.mixins.MinecraftClientAccessor;
import cis.matrixclient.util.render.Color;
import cis.matrixclient.util.render.Renderer;
import cis.matrixclient.util.world.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

@Module.Info(name = "Hud", category = Module.Category.CLIENT)
public class HUD extends Module {
    public enum ArraySort{
        Length,
        ABC
    }

    public enum Rendering{
        Up,
        Down
    }

    public static HUD get = new HUD();
    public Setting<Color> color = register("Color", new Color());
    public Setting<Boolean> watermark = register("Watermark", true);
    public Setting<Boolean> weeeed = register("420", false);
    public Setting<Boolean> arrayList = register("ArrayList", true);
    public Setting<ArraySort> arraySort = register("Sort", ArraySort.Length, ArraySort.values());
    public Setting<Boolean> direction = register("Direction", true);
    public Setting<Boolean> coords = register("Coords", true);
    public Setting<Boolean> netherCoords = register("NetherCoords", true);
    public Setting<Boolean> speed = register("Speed", true);
    public Setting<Boolean> durability = register("Durability", true);
    public Setting<Boolean> ping = register("Ping", false);
    public Setting<Boolean> TPS = register("TPS", false);
    public Setting<Boolean> FPS = register("FPS", true);
    public Setting<Boolean> armor = register("Armor", false);
    public Setting<Rendering> rendering = register("Rendering", Rendering.Up, Rendering.values());

    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long timeLastTimeUpdate = -1;
    private long timeGameJoined;

    public HUD() {
        get = this;
    }

    private static double animation(double current, double end, double factor, double start) {
        double progress = (end - current) * factor;

        if (progress > 0) {
            progress = Math.max(start, progress);
            progress = Math.min(end - current, progress);
        } else if (progress < 0) {
            progress = Math.min(-start, progress);
            progress = Math.max(end - current, progress);
        }

        return current + progress;
    }

    @Subscribe
    public void onRender(Render2DEvent event) {
        if (!WorldUtils.canUpdate()) return;
        int arrayModules = 0;
        int hudElements = 0;

        MatrixStack matrixstack = new MatrixStack();

        if (armor.get()) {
            double x = mc.getWindow().getScaledWidth() / 2 + 7 * mc.getWindow().getScaleFactor();
            double y = mc.getWindow().getScaledHeight() - 28 * mc.getWindow().getScaleFactor();
            double armorX;
            double armorY;
            int slot = 3;

            for (int position = 0; position < 4; position++) {
                ItemStack itemStack = mc.player.getInventory().getArmorStack(slot);

                armorX = x + position * 18;
                armorY = y;

                Renderer.drawItem(itemStack, (int) armorX, (int) armorY, 1, true);
                if (itemStack.isDamageable()) {
                    final float g = (itemStack.getMaxDamage() - (float) itemStack.getDamage()) / itemStack.getMaxDamage();
                    final float r = 1.0f - g;
                    String text = Integer.toString(Math.round(((itemStack.getMaxDamage() - itemStack.getDamage()) * 100f) / (float) itemStack.getMaxDamage()));
                    mc.textRenderer.drawWithShadow(matrixstack, text, (float) armorX + 1, (float) armorY - 10, new java.awt.Color(r, g, 0).getRGB());
                }
                slot--;
            }
        }

        if (coords.get()) {
            mc.textRenderer.drawWithShadow(matrixstack,
                    "XYZ " + Formatting.WHITE +
                            getString(mc.player.getX()) +
                            Formatting.GRAY + ", " + Formatting.WHITE +
                            getString(mc.player.getY()) +
                            Formatting.GRAY + ", " + Formatting.WHITE +
                            getString(mc.player.getZ()) +
                            (netherCoords.get() ? Formatting.GRAY + " [" + Formatting.WHITE +
                                    getString(worldCords(mc.player.getX())) +
                                    Formatting.GRAY + ", " + Formatting.WHITE +
                                    getString(worldCords(mc.player.getZ())) + Formatting.GRAY + "]" : ""),
                    2, mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - (mc.currentScreen instanceof ChatScreen ? 15 : 1), color());
        }

        if (direction.get()) {
            mc.textRenderer.drawWithShadow(matrixstack, getFacing() + Formatting.GRAY + " [" + Formatting.WHITE + getTowards() + Formatting.GRAY + "]",
                    2, mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight * (coords.get() ? 2 : 1) - (mc.currentScreen instanceof ChatScreen ? 15 : 1), color());

        }

        if (watermark.get()) {
            mc.textRenderer.drawWithShadow(matrixstack,
                    "MatrixClient" + (weeeed.get() ? "420" : " "), 2, 2, color());
        }

        if (arrayList.get()) {
            int[] counter = {1};
            // Formatting.GRAY + " [" + Formatting.WHITE + info + Formatting.GRAY + "]"
            ArrayList<Module> modules = new ArrayList<>(ModuleManager.getModules());
            switch (arraySort.get()) {
                case Length:
                    modules.sort(Comparator.comparing(m -> -(mc.textRenderer.getWidth(m.getName()) + mc.textRenderer.getWidth(m.getInfo()))));
                case ABC:
                    modules.sort(Comparator.comparing(m -> getName() + m.getInfo()));
            }
            for (Module value : modules) {
                Module module;
                module = value;
                if (module.drawn && module.enabled) {
                    int x = mc.getWindow().getScaledWidth();
                    int y = rendering.get(Rendering.Up) ? 3 + (arrayModules * 10) : mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - 1 - (arrayModules * 10);
                    int lWidth = mc.textRenderer.getWidth(module.getName() + (module.getInfo().equals("") ? "" : " [" + module.getInfo() + "]"));

                    if (mc.currentScreen instanceof ChatScreen && rendering.get(Rendering.Down)) y = y - 15;

                    if (module.arrayAnimation < lWidth && module.enabled) {
                        module.arrayAnimation = animation(module.arrayAnimation, lWidth + 5, 1, 0.1);
                    } else if (module.arrayAnimation > 1.5f && !module.enabled) {
                        module.arrayAnimation = animation(module.arrayAnimation, -1.5, 1, 0.1);
                    } else if (module.arrayAnimation <= 1.5f && !module.enabled) {
                        module.arrayAnimation = -1f;
                    }
                    if (module.arrayAnimation > lWidth && module.enabled) {
                        module.arrayAnimation = lWidth;
                    }
                    // TODO: 23.04.2022 свой FontRender, без него анимацию в аррай листе не сделать.
                    x -= module.arrayAnimation;


                    mc.textRenderer.drawWithShadow(matrixstack, module.getName() + (module.getInfo().equals("") ? "" : Formatting.GRAY + " [" + Formatting.WHITE + module.getInfo() + Formatting.GRAY + "]"), x - 2, y, color());

                    arrayModules++;
                    counter[0]++;
                }
            }
        }

        if (speed.get()) {
            int x = mc.getWindow().getScaledWidth();
            int y = rendering.get(Rendering.Up) ? mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - 1 - (hudElements * 10) : 3 + (hudElements * 10);
            double speed = getSpeed();

            if (mc.currentScreen instanceof ChatScreen && rendering.get(Rendering.Up)) y = y - 15;

            String text = "Speed " + Formatting.WHITE + getString(speed) + "km/h";

            float x2 = (float) (x - mc.textRenderer.getWidth(text) - 2);
            mc.textRenderer.drawWithShadow(matrixstack, text,
                    x2, y, color());

            hudElements++;
        }

        if (durability.get()) {
            if (!mc.player.getMainHandStack().isEmpty() && mc.player.getMainHandStack().isDamageable()) {
                int x = mc.getWindow().getScaledWidth();
                int y = rendering.get(Rendering.Up) ? mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - 1 - (hudElements * 10) : 3 + (hudElements * 10);
                if (mc.currentScreen instanceof ChatScreen && rendering.get(Rendering.Up)) y = y - 15;

                ItemStack stack = mc.player.getMainHandStack();

                String content = String.valueOf(stack.getMaxDamage() - stack.getDamage());

                final float g = (stack.getMaxDamage() - (float) stack.getDamage()) / stack.getMaxDamage();
                final float r = 1.0f - g;

                String durability = "Durability ";
                String text = content;

                float x2 = (float) (x -  mc.textRenderer.getWidth(durability + text) - 2);
                mc.textRenderer.drawWithShadow(matrixstack, durability,
                        x2, y, color());
                mc.textRenderer.drawWithShadow(matrixstack, text,
                        (float) (x - mc.textRenderer.getWidth(text) - 2), y, new java.awt.Color(r, g, 0).getRGB());

                hudElements++;
            }
        }

        if (ping.get()) {
            int x = mc.getWindow().getScaledWidth();
            int y = rendering.get(Rendering.Up) ? mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - 1 - (hudElements * 10) : 3 + (hudElements * 10);
            if (mc.currentScreen instanceof ChatScreen && rendering.get(Rendering.Up)) y = y - 15;

            PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());

            String content = "Ping " + Formatting.WHITE + playerListEntry.getLatency();

            float x2 = (float) (x - mc.textRenderer.getWidth(content) - 2);
            mc.textRenderer.drawWithShadow(matrixstack, content,
                    x2, y, color());

            hudElements++;
        }

        if (TPS.get()) {
            int x = mc.getWindow().getScaledWidth();
            int y = rendering.get(Rendering.Up) ? mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - 1 - (hudElements * 10) : 3 + (hudElements * 10);
            if (mc.currentScreen instanceof ChatScreen && rendering.get(Rendering.Up)) y = y - 15;

            String content = "TPS " + Formatting.WHITE + String.format("%.1f", getTickRate());

            float x2 = (float) (x - mc.textRenderer.getWidth(content) - 2);
            mc.textRenderer.drawWithShadow(matrixstack, content,
                    x2, y, color());

            hudElements++;
        }

        if (FPS.get()) {
            int x = mc.getWindow().getScaledWidth();
            int y = rendering.get(Rendering.Up) ? mc.getWindow().getScaledHeight() - mc.textRenderer.fontHeight - 1 - (hudElements * 10) : 3 + (hudElements * 10);
            if (mc.currentScreen instanceof ChatScreen && rendering.get(Rendering.Up)) y = y - 15;

            String content = "FPS " + Formatting.WHITE + MinecraftClientAccessor.getFps();

            float x2 = (float) (x - mc.textRenderer.getWidth(content) - 2);
            mc.textRenderer.drawWithShadow(matrixstack, content, x2, y, color());

            hudElements++;
        }
    }

    @Subscribe
    public void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof WorldTimeUpdateS2CPacket) {
            long now = System.currentTimeMillis();
            float timeElapsed = (float) (now - timeLastTimeUpdate) / 1000.0F;
            tickRates[nextIndex] = clamp(20.0f / timeElapsed, 0.0f, 20.0f);
            nextIndex = (nextIndex + 1) % tickRates.length;
            timeLastTimeUpdate = now;
        }
    }

    @Subscribe
    private void onGameJoined(GameJoinedEvent event) {
        Arrays.fill(tickRates, 0);
        nextIndex = 0;
        timeGameJoined = timeLastTimeUpdate = System.currentTimeMillis();
    }

    public float getTickRate() {
        if (mc.world == null || mc.player == null) return 0;
        if (System.currentTimeMillis() - timeGameJoined < 4000) return 20;

        int numTicks = 0;
        float sumTickRates = 0.0f;
        for (float tickRate : tickRates) {
            if (tickRate > 0) {
                sumTickRates += tickRate;
                numTicks++;
            }
        }
        return sumTickRates / numTicks;
    }

    public float getTimeSinceLastTick() {
        long now = System.currentTimeMillis();
        if (now - timeGameJoined < 4000) return 0;
        return (now - timeLastTimeUpdate) / 1000f;
    }


    private float clamp(float value, float min, float max) {
        if (value < min) return min;
        return Math.min(value, max);
    }

    private String getString(double pos) {
        return String.format("%.1f", pos).replace(',', '.');
    }

    private String getFacing() {
        return switch (MathHelper.floor((double) (mc.player.getYaw() * 4.0F / 360.0F) + 0.5D) & 3) {
            case 0 -> "South";
            case 1 -> "West";
            case 2 -> "North";
            case 3 -> "East";
            default -> "Invalid";
        };
    }

    private double worldCords(double cords) {
        if (mc.world.getDimension().respawnAnchorWorks()) return (cords * 8);
        return cords / 8;
    }

    private String getTowards() {
        return switch (MathHelper.floor((double) (mc.player.getYaw() * 4.0F / 360.0F) + 0.5D) & 3) {
            case 0 -> "+Z";
            case 1 -> "-X";
            case 2 -> "-Z";
            case 3 -> "+X";
            default -> "Invalid";
        };
    }

    private double getSpeed() {
        if (mc.player == null) return 0;

        double tX = Math.abs(mc.player.getX() - mc.player.prevX);
        double tZ = Math.abs(mc.player.getZ() - mc.player.prevZ);
        double length = Math.sqrt(tX * tX + tZ * tZ);

//        Timer timer = Modules.get().get(Timer.class);
//        if (timer.isActive()) length *= Modules.get().get(Timer.class).getMultiplier();

        return (length * 20) * 3.6;
    }

    public int color() {
        return color.get().getRgb();
    }
}
