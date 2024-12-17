package org.example;

import java.util.ArrayDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryQueue2 implements OrderQueue {

    public static final int DEFAULT_CAPACITY = 16;

    int capacity;
    ArrayDeque<Order> orders = new ArrayDeque<Order>();
    // See pthread_mutex_t and pthread_cond_t for underlying libc implementation.
    // Would be nice if java had a non-reentrant lock.
    final Lock lock = new ReentrantLock();
    Condition waitFull = lock.newCondition();
    Condition waitEmpty = lock.newCondition();

    /**
     * Create queue with maximum number of outstanding orders that can be created
     * before `createOrder()` blocks the calling thread until `consumeOrder()` makes
     * progress.
     */
    public InMemoryQueue2(int maxOutstanding) {
        if (maxOutstanding <= 0) {
            // TODO log warning
            maxOutstanding = DEFAULT_CAPACITY;
        }
        capacity = maxOutstanding;
    }

    @Override
    public void createOrder(Order order) throws OrderError, InterruptedException {
        // TODO validate order

        lock.lock();
        try {
            while (orders.size() >= capacity) {
                // Needs to be in a loop to protect against spurious wakeups.
                waitFull.await();
            }
            orders.addLast(order);
            // Use singular signal to avoid thundering herd contention.
            waitEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    // This differs from `InMemoryQueue` in that we block on consumer side (empty)
    // as well. In real life we'd want consistent behavior. This is just a demo.
    @Override
    public Order consumeOrder() throws OrderError, InterruptedException {
        lock.lock();
        try {
            while (orders.isEmpty()) {
                waitEmpty.await();
            }
            Order order = orders.removeFirst();
            // Use singular signal to avoid thundering herd contention.
            waitFull.signal();
            return order;
        } finally {
            lock.unlock();
        }
    }
}
