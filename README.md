Implement a simple producer/consumer queue which:

- Has a maximum capacity for produced-but-not-consumed entries.
- Blocks the consumer's thread when empty.
- Blocks the producer's thread when full.

Two implementations:

1. Using a semaphore to enforce capacity / blocking: [InMemoryQueue.java](app/src/main/java/org/example/InMemoryQueue.java).
2. Using a lock and condition variable instead: [InMemoryQueue2.java](app/src/main/java/org/example/InMemoryQueue2.java).

Note that the implementations differ in that #2 does not block consumer when
the queue is empty; it has to poll and try again.

## Test Output

```
$ ./gradlew test
...
TestOrderQueues > testLockCondQueue() STANDARD_OUT
    Elapsed time: 3441.807592 msec, 40000 orders, (86.0451898 usec/order)

TestOrderQueues > testSemaphoreQueue() STANDARD_OUT
    Elapsed time: 3275.931546 msec, 40000 orders, (81.89828864999998 usec/order)

```




