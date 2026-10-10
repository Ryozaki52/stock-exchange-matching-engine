package com.ryozaki52.matching;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MatchingEngine {

    private final Map<String, OrderBook> orderBooks;

    private final Set<Long> usedOrderIds;

    public MatchingEngine() {

        this.orderBooks =
                new ConcurrentHashMap<>();

        this.usedOrderIds =
                ConcurrentHashMap.newKeySet();
    }

    public List<Trade> submitOrder(Order order) {

        boolean reserved =
                usedOrderIds.add(
                        order.getOrderId()
                );

        if (!reserved) {
            throw new IllegalArgumentException(
                    "Duplicate global order ID: "
                            + order.getOrderId()
            );
        }

        try {

            String symbol =
                    order.getSymbol();

            OrderBook orderBook =
                    orderBooks.computeIfAbsent(
                            symbol,
                            OrderBook::new
                    );

            return orderBook.submitOrder(order);

        } catch (RuntimeException exception) {

            usedOrderIds.remove(
                    order.getOrderId()
            );

            throw exception;
        }
    }

    public boolean cancelOrder(
            String symbol,
            long orderId) {

        if (symbol == null
                || symbol.isBlank()) {

            return false;
        }

        OrderBook orderBook =
                orderBooks.get(
                        symbol.toUpperCase()
                );

        if (orderBook == null) {
            return false;
        }

        return orderBook.cancelOrder(
                orderId
        );
    }

    public OrderBook getOrderBook(
            String symbol) {

        if (symbol == null
                || symbol.isBlank()) {

            return null;
        }

        return orderBooks.get(
                symbol.toUpperCase()
        );
    }

    public int getSymbolCount() {
        return orderBooks.size();
    }

    public boolean hasUsedOrderId(
            long orderId) {

        return usedOrderIds.contains(
                orderId
        );
    }

    public int getUsedOrderIdCount() {
        return usedOrderIds.size();
    }
}