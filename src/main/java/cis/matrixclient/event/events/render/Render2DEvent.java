package cis.matrixclient.event.events.render;


import cis.matrixclient.event.Event;
import net.minecraft.client.util.math.MatrixStack;

public class Render2DEvent extends Event {
    public MatrixStack matrixStack;

    public Render2DEvent(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }
}
