package com.tank.staffcore.managers;

import java.util.concurrent.atomic.AtomicBoolean;

public class MutechatManager {

    private final AtomicBoolean muted = new AtomicBoolean(false);

    public boolean isMuted() {
        return muted.get();
    }

    public void setMuted(boolean value) {
        muted.set(value);
    }
}