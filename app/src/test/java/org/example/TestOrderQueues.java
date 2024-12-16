package org.example;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.example.OrderQueue.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestOrderQueues {
    public static final int NUM_PRODUCERS = 20;
    public static final int ORDERS_PER_PRODUCER = 2000;
    public static final int QUEUE_CAPACITY = 10;

    private ExecutorService exec;

    @BeforeEach
    public void setUp() {
        exec = Executors.newFixedThreadPool(NUM_PRODUCERS + 1);
    }

    @Test
    public void testSemaphoreQueue() throws OrderError, InterruptedException {
        testQueue(new InMemoryQueue(QUEUE_CAPACITY));
    }

    @Test
    public void testLockCondQueue() throws OrderError, InterruptedException {
        testQueue(new InMemoryQueue2(QUEUE_CAPACITY));
    }

    void testQueue(OrderQueue queue) throws OrderError, InterruptedException {
        // Create and start consumer
        CompletableFuture<Integer> consumerFut = CompletableFuture.supplyAsync(() -> {
            int numOrders = 0;
            while (true) {
                try {
                    // Small sleep to simulate computation, etc.
                    java.lang.Thread.sleep(0, 20 /* usec */);
                    Order order = queue.consumeOrder();
                    numOrders++;
                    if (numOrders == NUM_PRODUCERS * ORDERS_PER_PRODUCER) {
                        break;
                    }
                } catch (InterruptedException e) {
                    System.out.println("Consumer interrupted, shutting down");
                    Thread.currentThread().interrupt();
                } catch (OrderError e) {
                    System.out.println("Error consuming order: " + e.getMessage());
                    break;
                }
            }
            return numOrders;
        }, exec);

        // Create and start producers
        ArrayList<CompletableFuture<Void>> producers = new ArrayList<>(NUM_PRODUCERS);
        for (int p = 0; p < NUM_PRODUCERS; p++) {
            final int producer = p;
            CompletableFuture<Void> producerFut = CompletableFuture.supplyAsync(() -> {
                try {
                    for (int i = 0; i < ORDERS_PER_PRODUCER; i++) {
                        // Small sleep to simulate computation, etc.
                        java.lang.Thread.sleep(0, 10 /* usec */);
                        String customerId = "c-" + Integer.toString(producer) + "-" + Integer.toString(i);
                        queue.createOrder(new Order(customerId, "game", GameType.DIGITAL));
                    }
                } catch (InterruptedException e) {
                    System.out.println("Producer interrupted, shutting down");
                    Thread.currentThread().interrupt();
                } catch (OrderError e) {
                    System.out.println("Error creating order: " + e.getMessage());
                }
                return null;
            }, exec);
            producers.add(producerFut);
        }

        // Begin timing (benchmark)
        long start = System.nanoTime();

        // Wait for producers to finish
        for (CompletableFuture<Void> producer : producers) {
            producer.join();
        }

        // Fail if it takes more than 5 seconds for consumer to finish
        try {
            int numConsumed = consumerFut.get(5, java.util.concurrent.TimeUnit.SECONDS);
            assertEquals(NUM_PRODUCERS * ORDERS_PER_PRODUCER, numConsumed);
            // End timing (benchmark)
            double elapsedMsec = (System.nanoTime() - start) / 1e6;
            System.out.println("Elapsed time: " + elapsedMsec + " msec, " + numConsumed
                    + " orders, (" + elapsedMsec * 1000 / numConsumed + " usec/order)");
        } catch (java.util.concurrent.TimeoutException e) {
            fail("Consumer took too long to finish");
        } catch (java.util.concurrent.ExecutionException e) {
            fail("Consumer threw exception: " + e.getCause().getMessage());
        }

    }
}

