package org.example;

/**
 * Proxy for `InterruptedException`, e.g. when shutdown occurs while waiting to
 * submit an order, etc.
 */
public class ThreadInterrupted extends OrderError {
    public ThreadInterrupted() {
        super("Operation interrupted");
    }
}
