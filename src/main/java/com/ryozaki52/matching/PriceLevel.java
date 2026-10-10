package com.ryozaki52.matching;

public class PriceLevel {

    private final long priceInCents;
    private final int totalQuantity;

    public PriceLevel(long priceInCents, int totalQuantity) {
        this.priceInCents = priceInCents;
        this.totalQuantity = totalQuantity;
    }

    public long getPriceInCents() {
        return priceInCents;
    }

    public double getPriceAsDollars() {
        return priceInCents / 100.0;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }
}