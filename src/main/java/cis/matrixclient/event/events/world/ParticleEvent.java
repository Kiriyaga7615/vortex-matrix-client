/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package cis.matrixclient.event.events.world;

import cis.matrixclient.event.Event;
import net.minecraft.particle.ParticleEffect;

public class ParticleEvent extends Event {
    public ParticleEffect particle;

    public ParticleEvent(ParticleEffect particle) {
        this.setCancelled(false);
        this.particle = particle;
    }
}
