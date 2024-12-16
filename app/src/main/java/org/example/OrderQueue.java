package org.example;

public interface OrderQueue {

    public record Order(String customerId, String gameName, GameType gameType) {}

    void createOrder(Order order) throws OrderError;

    Order consumeOrder() throws OrderError;
}
