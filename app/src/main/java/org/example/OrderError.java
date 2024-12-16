package org.example;
public class OrderError extends Exception {
    public OrderError(String message) {
        super(message);
    }
}
