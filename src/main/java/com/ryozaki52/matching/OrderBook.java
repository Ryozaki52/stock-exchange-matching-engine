package com.ryozaki52.matching;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.TreeMap;

public class OrderBook {

    private final String symbol;

    private final NavigableMap<Long, Queue<Order>> buyOrders;
    private final NavigableMap<Long, Queue<Order>> sellOrders;

    private final List<Trade> tradeHistory;
    private final Map<Long, Order> activeOrders;
    private long nextSequenceNumber;
    public OrderBook(String symbol) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be blank");
        }

        this.symbol = symbol.toUpperCase();

        this.buyOrders =
                new TreeMap<>(Comparator.reverseOrder());

        this.sellOrders =
                new TreeMap<>();

        this.tradeHistory =
                new ArrayList<>();

        this.activeOrders =new HashMap<>();
        this.nextSequenceNumber = 1;
    }

    public String getSymbol() {
        return symbol;
    }

    public synchronized void addOrder(Order order) {


        if (!order.getSymbol().equals(symbol)) {
            throw new IllegalArgumentException(
                    "Order symbol does not match this order book"
            );

        }

        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalArgumentException(
                    "Only NEW orders can be added"
            );
        }

        if (activeOrders.containsKey(order.getOrderId())) {
            throw new IllegalArgumentException(
                    "Duplicate order ID: "
                            + order.getOrderId()
            );

        }
        order.assignSequenceNumber(
                nextSequenceNumber++
        );
        NavigableMap<Long, Queue<Order>> bookSide =
                order.getSide() == OrderSide.BUY
                        ? buyOrders
                        : sellOrders;

        bookSide
                .computeIfAbsent(
                        order.getPriceInCents(),
                        price -> new ArrayDeque<>()
                )
                .add(order);

        activeOrders.put(
                order.getOrderId(),
                order
        );


    }
    public synchronized List<Trade> submitOrder(Order order) {

        addOrder(order);

        return matchOrders();
    }
    public synchronized List<Trade> matchOrders() {

        List<Trade> newTrades =
                new ArrayList<>();

        while (!buyOrders.isEmpty()
                && !sellOrders.isEmpty()) {

            long bestBid =
                    buyOrders.firstKey();

            long bestAsk =
                    sellOrders.firstKey();

            if (bestBid < bestAsk) {
                break;
            }

            Queue<Order> buyQueue =
                    buyOrders.get(bestBid);

            Queue<Order> sellQueue =
                    sellOrders.get(bestAsk);

            Order buyOrder =
                    buyQueue.peek();

            Order sellOrder =
                    sellQueue.peek();

            int tradeQuantity =
                    Math.min(
                            buyOrder.getRemainingQuantity(),
                            sellOrder.getRemainingQuantity()
                    );

            long tradePriceInCents;

            if (buyOrder.getSequenceNumber()
                    < sellOrder.getSequenceNumber()) {

                tradePriceInCents =
                        buyOrder.getPriceInCents();

            } else {

                tradePriceInCents =
                        sellOrder.getPriceInCents();
            }
            buyOrder.reduceQuantity(
                    tradeQuantity
            );

            sellOrder.reduceQuantity(
                    tradeQuantity
            );

            Trade trade =
                    new Trade(
                            buyOrder.getOrderId(),
                            sellOrder.getOrderId(),
                            symbol,
                            tradePriceInCents,
                            tradeQuantity
                    );

            tradeHistory.add(trade);
            newTrades.add(trade);

            if (buyOrder.isFilled()) {

                buyQueue.poll();

                activeOrders.remove(
                        buyOrder.getOrderId()
                );
            }

            if (sellOrder.isFilled()) {

                sellQueue.poll();

                activeOrders.remove(
                        sellOrder.getOrderId()
                );
            }

            if (buyQueue.isEmpty()) {
                buyOrders.remove(bestBid);
            }

            if (sellQueue.isEmpty()) {
                sellOrders.remove(bestAsk);
            }
        }

        return newTrades;
    }
    public synchronized List<PriceLevel> getBuyDepth() {

        List<PriceLevel> depth = new ArrayList<>();

        for (var entry : buyOrders.entrySet()) {

            int totalQuantity = entry.getValue()
                    .stream()
                    .mapToInt(Order::getRemainingQuantity)
                    .sum();

            depth.add(
                    new PriceLevel(
                            entry.getKey(),
                            totalQuantity
                    )
            );
        }

        return depth;
    }

    public synchronized List<PriceLevel> getSellDepth() {

        List<PriceLevel> depth = new ArrayList<>();

        for (var entry : sellOrders.entrySet()) {

            int totalQuantity = entry.getValue()
                    .stream()
                    .mapToInt(Order::getRemainingQuantity)
                    .sum();

            depth.add(
                    new PriceLevel(
                            entry.getKey(),
                            totalQuantity
                    )
            );
        }

        return depth;
    }

    public synchronized boolean cancelOrder(long orderId) {

        Order order =
                activeOrders.get(orderId);

        if (order == null) {
            return false;
        }

        NavigableMap<Long, Queue<Order>> bookSide =
                order.getSide() == OrderSide.BUY
                        ? buyOrders
                        : sellOrders;

        Queue<Order> queue =
                bookSide.get(
                        order.getPriceInCents()
                );

        if (queue == null) {
            return false;
        }

        boolean removed =
                queue.remove(order);

        if (!removed) {
            return false;
        }

        order.cancel();

        activeOrders.remove(orderId);

        if (queue.isEmpty()) {
            bookSide.remove(
                    order.getPriceInCents()
            );
        }

        return true;
    }

    public synchronized boolean hasActiveOrder(long orderId){
        return activeOrders.containsKey(orderId);
    }

    public synchronized int getActiveOrderCount() {
        return activeOrders.size();
    }

    public synchronized Long getBestBid(){

        if (buyOrders.isEmpty()) {
            return null;
        }

        return buyOrders.firstKey();
    }

    public synchronized Long getBestAsk() {

        if (sellOrders.isEmpty()) {
            return null;
        }

        return sellOrders.firstKey();
    }

    public synchronized List<Trade> getTradeHistory() {
        return List.copyOf(tradeHistory);
    }
}