package com.pizza;

import java.util.function.Consumer;

public class TimeoutTimer extends Thread {
    private final long timeout;
    private final Consumer<String[]> onTimeout;

    private final String message;

    public TimeoutTimer(long timeout, Consumer<String[]> onTimeout, String message) {
        this.timeout = timeout;
        this.onTimeout = onTimeout;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(timeout);
            onTimeout.accept(new String[]{"Temps d'attente dépassé", message});
        } catch (InterruptedException e) {
            // ignored
        }
    }
}
