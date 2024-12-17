package org.example;

public interface OrderQueue {

    public record Order(String customerId, String gameName, GameType gameType) {}

    void createOrder(Order order) throws OrderError, InterruptedException;

    Order consumeOrder() throws OrderError, InterruptedException;
}
