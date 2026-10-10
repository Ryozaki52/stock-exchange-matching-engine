package com.ryozaki52.matching;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {

    @Test
    void shouldAutomaticallyMatchSubmittedOrders() {

        MatchingEngine engine =
                new MatchingEngine();

        Order buyOrder = new Order(
                1,
                "AAPL",
                OrderSide.BUY,
                19000,
                100
        );

        Order sellOrder = new Order(
                2,
                "AAPL",
                OrderSide.SELL,
                18500,
                40
        );

        List<Trade> firstSubmission =
                engine.submitOrder(buyOrder);

        assertTrue(firstSubmission.isEmpty());

        List<Trade> secondSubmission =
                engine.submitOrder(sellOrder);

        assertEquals(
                1,
                secondSubmission.size()
        );

        Trade trade =
                secondSubmission.get(0);

        assertEquals(
                40,
                trade.getQuantity()
        );

        // BUY was resting first
        assertEquals(
                19000L,
                trade.getPriceInCents()
        );

        assertEquals(
                OrderStatus.PARTIALLY_FILLED,
                buyOrder.getStatus()
        );

        assertEquals(
                OrderStatus.FILLED,
                sellOrder.getStatus()
        );
    }

    @Test
    void shouldKeepDifferentSymbolsIndependent() {

        MatchingEngine engine =
                new MatchingEngine();

        Order aaplBuy = new Order(
                1,
                "AAPL",
                OrderSide.BUY,
                19000,
                100
        );

        Order tslaSell = new Order(
                2,
                "TSLA",
                OrderSide.SELL,
                35000,
                50
        );

        engine.submitOrder(aaplBuy);
        engine.submitOrder(tslaSell);

        assertEquals(
                2,
                engine.getSymbolCount()
        );

        OrderBook aaplBook =
                engine.getOrderBook("AAPL");

        OrderBook tslaBook =
                engine.getOrderBook("TSLA");

        assertNotNull(aaplBook);
        assertNotNull(tslaBook);

        assertEquals(
                19000L,
                aaplBook.getBestBid()
        );

        assertNull(
                aaplBook.getBestAsk()
        );

        assertNull(
                tslaBook.getBestBid()
        );

        assertEquals(
                35000L,
                tslaBook.getBestAsk()
        );
    }

    @Test
    void shouldCancelOrderThroughMatchingEngine() {

        MatchingEngine engine =
                new MatchingEngine();

        Order buyOrder = new Order(
                1,
                "AAPL",
                OrderSide.BUY,
                18500,
                100
        );

        engine.submitOrder(buyOrder);

        boolean cancelled =
                engine.cancelOrder(
                        "AAPL",
                        1
                );

        assertTrue(cancelled);

        assertEquals(
                OrderStatus.CANCELLED,
                buyOrder.getStatus()
        );

        assertFalse(
                engine.getOrderBook("AAPL")
                        .hasActiveOrder(1)
        );
    }
    @Test
    void shouldRejectDuplicateOrderIdAcrossSymbols() {

        MatchingEngine engine =
                new MatchingEngine();

        Order aaplOrder = new Order(
                1,
                "AAPL",
                OrderSide.BUY,
                19000,
                100
        );

        Order tslaOrder = new Order(
                1,
                "TSLA",
                OrderSide.SELL,
                35000,
                50
        );

        engine.submitOrder(aaplOrder);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> engine.submitOrder(tslaOrder)
                );

        assertEquals(
                "Duplicate global order ID: 1",
                exception.getMessage()
        );

        assertEquals(
                1,
                engine.getUsedOrderIdCount()
        );

        assertFalse(
                engine.hasUsedOrderId(2)
        );
    }
    @Test
    void shouldNotReuseOrderIdAfterOrderIsFilled() {

        MatchingEngine engine =
                new MatchingEngine();

        Order buyOrder = new Order(
                1,
                "AAPL",
                OrderSide.BUY,
                19000,
                40
        );

        Order sellOrder = new Order(
                2,
                "AAPL",
                OrderSide.SELL,
                18500,
                40
        );

        engine.submitOrder(buyOrder);
        engine.submitOrder(sellOrder);

        assertEquals(
                OrderStatus.FILLED,
                buyOrder.getStatus()
        );

        Order reusedId = new Order(
                1,
                "NVDA",
                OrderSide.BUY,
                20000,
                10
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.submitOrder(reusedId)
        );
    }
    @Test
    void shouldHandleConcurrentOrdersForSameSymbol() throws Exception {

        MatchingEngine engine =
                new MatchingEngine();

        int ordersPerSide = 1000;
        int totalOrders = ordersPerSide * 2;

        ExecutorService executor =
                Executors.newFixedThreadPool(8);

        CountDownLatch startGate =
                new CountDownLatch(1);

        List<Future<?>> futures =
                new ArrayList<>();

        try {

            for (int i = 1; i <= ordersPerSide; i++) {

                long buyOrderId = i;

                futures.add(
                        executor.submit(() -> {

                            startGate.await();

                            Order buyOrder =
                                    new Order(
                                            buyOrderId,
                                            "AAPL",
                                            OrderSide.BUY,
                                            10100,
                                            1
                                    );

                            engine.submitOrder(
                                    buyOrder
                            );

                            return null;
                        })
                );
            }

            for (int i = 1; i <= ordersPerSide; i++) {

                long sellOrderId =
                        ordersPerSide + i;

                futures.add(
                        executor.submit(() -> {

                            startGate.await();

                            Order sellOrder =
                                    new Order(
                                            sellOrderId,
                                            "AAPL",
                                            OrderSide.SELL,
                                            9900,
                                            1
                                    );

                            engine.submitOrder(
                                    sellOrder
                            );

                            return null;
                        })
                );
            }

            // Release all worker threads
            startGate.countDown();

            // Wait until every submitted task finishes
            for (Future<?> future : futures) {
                future.get();
            }

        } finally {

            executor.shutdown();
        }

        OrderBook aaplBook =
                engine.getOrderBook("AAPL");

        assertNotNull(aaplBook);

        assertEquals(
                totalOrders,
                engine.getUsedOrderIdCount()
        );

        assertEquals(
                0,
                aaplBook.getActiveOrderCount()
        );

        assertEquals(
                ordersPerSide,
                aaplBook.getTradeHistory().size()
        );

        assertNull(
                aaplBook.getBestBid()
        );

        assertNull(
                aaplBook.getBestAsk()
        );
    }
    @Test
    void shouldHandleConcurrentOrdersAcrossMultipleSymbols()
            throws Exception {

        MatchingEngine engine =
                new MatchingEngine();

        String[] symbols = {
                "AAPL",
                "TSLA",
                "NVDA",
                "MSFT"
        };

        int ordersPerSidePerSymbol = 250;

        ExecutorService executor =
                Executors.newFixedThreadPool(8);

        CountDownLatch startGate =
                new CountDownLatch(1);

        List<Future<?>> futures =
                new ArrayList<>();

        try {

            for (int symbolIndex = 0;
                 symbolIndex < symbols.length;
                 symbolIndex++) {

                String symbol =
                        symbols[symbolIndex];

                long idBase =
                        (long) symbolIndex
                                * ordersPerSidePerSymbol
                                * 2;

                for (int i = 1;
                     i <= ordersPerSidePerSymbol;
                     i++) {

                    long buyOrderId =
                            idBase + i;

                    long sellOrderId =
                            idBase
                                    + ordersPerSidePerSymbol
                                    + i;

                    futures.add(
                            executor.submit(() -> {

                                startGate.await();

                                Order buyOrder =
                                        new Order(
                                                buyOrderId,
                                                symbol,
                                                OrderSide.BUY,
                                                10100,
                                                1
                                        );

                                engine.submitOrder(
                                        buyOrder
                                );

                                return null;
                            })
                    );

                    futures.add(
                            executor.submit(() -> {

                                startGate.await();

                                Order sellOrder =
                                        new Order(
                                                sellOrderId,
                                                symbol,
                                                OrderSide.SELL,
                                                9900,
                                                1
                                        );

                                engine.submitOrder(
                                        sellOrder
                                );

                                return null;
                            })
                    );
                }
            }

            startGate.countDown();

            for (Future<?> future : futures) {
                future.get();
            }

        } finally {

            executor.shutdown();
        }

        int totalOrders =
                symbols.length
                        * ordersPerSidePerSymbol
                        * 2;

        assertEquals(
                4,
                engine.getSymbolCount()
        );

        assertEquals(
                totalOrders,
                engine.getUsedOrderIdCount()
        );

        for (String symbol : symbols) {

            OrderBook orderBook =
                    engine.getOrderBook(symbol);

            assertNotNull(orderBook);

            assertEquals(
                    0,
                    orderBook.getActiveOrderCount()
            );

            assertEquals(
                    ordersPerSidePerSymbol,
                    orderBook.getTradeHistory().size()
            );

            assertNull(
                    orderBook.getBestBid()
            );

            assertNull(
                    orderBook.getBestAsk()
            );
        }
    }
}