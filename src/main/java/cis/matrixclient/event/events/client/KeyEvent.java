/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package cis.matrixclient.event.events.client;

import cis.matrixclient.event.Event;
import cis.matrixclient.util.key.KeyBinds;

public class KeyEvent extends Event {
    public int key;
    public KeyBinds.Action action;

    public KeyEvent(int key, KeyBinds.Action action) {
        this.key = key;
        this.action = action;
    }
}
