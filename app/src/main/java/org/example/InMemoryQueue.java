package org.example;

import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;

public class InMemoryQueue implements OrderQueue {

    public static final int DEFAULT_CAPACITY = 16;

    int capacity;
    ArrayDeque<Order> orders = new ArrayDeque<Order>();

    Semaphore queuePermits;

    /**
     * Create queue with maximum number of outstanding orders that can be created
     * before `createOrder()` blocks the calling thread until `consumeOrder()` makes
     * progress.
     */
    public InMemoryQueue(int maxOutstanding) {
        if (maxOutstanding <= 0) {
            // TODO log warning
            maxOutstanding = DEFAULT_CAPACITY;
        }
        capacity = maxOutstanding;
        queuePermits = new Semaphore(capacity);
    }

    @Override
    public void createOrder(Order order) throws OrderError {
        // TODO validate order

        try {
            queuePermits.acquire();
        } catch (InterruptedException e) {
            throw new ThreadInterrupted();
        }
        // Counting semaphore with capacity > 1 doesn't give us mutual exclusion, so we
        // need a synchronized block for safe concurrent access to underlying data
        // structure.
        synchronized (this) {
            orders.addLast(order);
        }
    }

    @Override
    public Order consumeOrder() throws OrderError {
        Order order = null;
        synchronized (this) {
            if (!orders.isEmpty()) {
                order = orders.removeFirst();
            }
        }
        queuePermits.release();
        return order;
    }
}
