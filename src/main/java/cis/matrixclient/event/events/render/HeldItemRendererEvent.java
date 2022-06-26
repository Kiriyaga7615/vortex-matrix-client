/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package cis.matrixclient.event.events.render;

import cis.matrixclient.event.Event;
import net.minecraft.client.util.math.MatrixStack;

public class HeldItemRendererEvent extends Event {
    public static class Hand extends HeldItemRendererEvent {
        public net.minecraft.util.Hand hand;
        public MatrixStack matrix;

        public Hand(net.minecraft.util.Hand hand, MatrixStack matrices) {
            this.hand = hand;
            this.matrix = matrices;

        }
    }

    public static class Update extends HeldItemRendererEvent  {

    }
}