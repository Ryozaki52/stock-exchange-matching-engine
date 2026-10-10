package com.ryozaki52.matching;

import java.time.LocalDateTime;

public class Trade {

    private final long buyOrderId;
    private final long sellOrderId;
    private final String symbol;
    private final long priceInCents;
    private final int quantity;
    private final LocalDateTime timestamp;

    public Trade(long buyOrderId,
                 long sellOrderId,
                 String symbol,
                 long priceInCents,
                 int quantity) {

        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.symbol = symbol;
        this.priceInCents = priceInCents;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
    }

    public long getBuyOrderId() {
        return buyOrderId;
    }

    public long getSellOrderId() {
        return sellOrderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getPriceInCents() {
        return priceInCents;
    }

    public double getPriceAsDollars() {
        return priceInCents / 100.0;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}