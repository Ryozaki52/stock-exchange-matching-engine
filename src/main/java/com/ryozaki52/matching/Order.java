package com.ryozaki52.matching;

public class Order {

    private final long orderId;
    private final String symbol;
    private final OrderSide side;
    private final long priceInCents;
    private final int originalQuantity;
    private long sequenceNumber = -1;
    private int remainingQuantity;
    private OrderStatus status;

    public Order(long orderId,
                 String symbol,
                 OrderSide side,
                 long priceInCents,
                 int quantity) {

        if (orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be blank");
        }

        if (side == null) {
            throw new IllegalArgumentException("Order side cannot be null");
        }

        if (priceInCents <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than 0"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0"
            );
        }

        this.orderId = orderId;
        this.symbol = symbol.toUpperCase();
        this.side = side;
        this.priceInCents = priceInCents;
        this.originalQuantity = quantity;
        this.remainingQuantity = quantity;
        this.status = OrderStatus.NEW;
    }
    public long getSequenceNumber() {
        return sequenceNumber;
    }
    void assignSequenceNumber(long sequenceNumber) {

        if (this.sequenceNumber != -1) {
            throw new IllegalStateException(
                    "Sequence number already assigned"
            );
        }

        this.sequenceNumber = sequenceNumber;
    }
    public long getOrderId() {
        return orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public long getPriceInCents() {
        return priceInCents;
    }

    public double getPriceAsDollars() {
        return priceInCents / 100.0;
    }

    public int getOriginalQuantity() {
        return originalQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public OrderStatus getStatus() {
        return status;
    }


    public void reduceQuantity(int quantity) {

        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot fill a cancelled order"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity to reduce must be greater than 0"
            );
        }

        if (quantity > remainingQuantity) {
            throw new IllegalArgumentException(
                    "Cannot reduce more than remaining quantity"
            );
        }

        remainingQuantity -= quantity;

        if (remainingQuantity == 0) {
            status = OrderStatus.FILLED;
        } else {
            status = OrderStatus.PARTIALLY_FILLED;
        }
    }


    public void cancel() {

        if (status == OrderStatus.FILLED) {
            throw new IllegalStateException(
                    "Cannot cancel a filled order"
            );
        }

        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Order is already cancelled"
            );
        }

        status = OrderStatus.CANCELLED;
    }


    public boolean isFilled() {
        return status == OrderStatus.FILLED;
    }
}