package com.seeths.projects;

import java.time.Instant;

public class TradingEvent {
    private String symbol;
    private double price;
    private int quantity;
    private String side; // "BUY" or "SELL"
    private Instant timestamp;

    public TradingEvent() {}

    public TradingEvent(String symbol, double price, int quantity, String side, Instant timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.side = side;
        this.timestamp = timestamp;
    }

    // Getters and setters...

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}