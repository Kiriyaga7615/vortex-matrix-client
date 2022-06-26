package cis.matrixclient.event.events.render;

import cis.matrixclient.event.Event;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class EntityLabelRender extends Event {
    protected Entity entity;
    protected MatrixStack matrices;
    protected VertexConsumerProvider vertex;

    public Entity getEntity() {
        return entity;
    }

    public MatrixStack getMatrix() {
        return matrices;
    }

    public VertexConsumerProvider getVertex() {
        return vertex;
    }

    public EntityLabelRender(Entity entity, MatrixStack matrices, VertexConsumerProvider vertex) {
        this.entity = entity;
        this.matrices = matrices;
        this.vertex = vertex;
    }

    public void setMatrix(MatrixStack matrices) {
        this.matrices = matrices;
    }

    public void setVertex(VertexConsumerProvider vertex) {
        this.vertex = vertex;
    }
}
