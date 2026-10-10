package com.ryozaki52.matching;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {

    @Test
    void shouldMatchCrossingBuyAndSellOrders() {

        OrderBook orderBook =
                new OrderBook("AAPL");

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

        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);

        List<Trade> trades =
                orderBook.matchOrders();

        assertEquals(
                1,
                trades.size()
        );

        Trade trade =
                trades.get(0);

        assertEquals(
                40,
                trade.getQuantity()
        );

        assertEquals(
                19000L,
                trade.getPriceInCents()
        );

        assertEquals(
                60,
                buyOrder.getRemainingQuantity()
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
    void shouldUseBetterPriceBeforeEarlierWorsePrice() {

        OrderBook orderBook =
                new OrderBook("AAPL");

        Order sellWorsePrice = new Order(
                1,
                "AAPL",
                OrderSide.SELL,
                19000,
                50
        );

        Order sellBetterPrice = new Order(
                2,
                "AAPL",
                OrderSide.SELL,
                18500,
                50
        );

        Order buyOrder = new Order(
                3,
                "AAPL",
                OrderSide.BUY,
                20000,
                100
        );

        orderBook.addOrder(sellWorsePrice);
        orderBook.addOrder(sellBetterPrice);
        orderBook.addOrder(buyOrder);

        List<Trade> trades =
                orderBook.matchOrders();

        assertEquals(
                2,
                trades.size()
        );

        assertEquals(
                2,
                trades.get(0).getSellOrderId()
        );

        assertEquals(
                18500L,
                trades.get(0).getPriceInCents()
        );

        assertEquals(
                1,
                trades.get(1).getSellOrderId()
        );

        assertEquals(
                19000L,
                trades.get(1).getPriceInCents()
        );
    }

    @Test
    void shouldUseFifoForOrdersAtSamePrice() {

        OrderBook orderBook =
                new OrderBook("AAPL");

        Order sell1 = new Order(
                1,
                "AAPL",
                OrderSide.SELL,
                18500,
                50
        );

        Order sell2 = new Order(
                2,
                "AAPL",
                OrderSide.SELL,
                18500,
                70
        );

        Order buyOrder = new Order(
                3,
                "AAPL",
                OrderSide.BUY,
                18500,
                60
        );

        orderBook.addOrder(sell1);
        orderBook.addOrder(sell2);
        orderBook.addOrder(buyOrder);

        List<Trade> trades =
                orderBook.matchOrders();

        assertEquals(
                2,
                trades.size()
        );

        assertEquals(
                1,
                trades.get(0).getSellOrderId()
        );

        assertEquals(
                50,
                trades.get(0).getQuantity()
        );

        assertEquals(
                2,
                trades.get(1).getSellOrderId()
        );

        assertEquals(
                10,
                trades.get(1).getQuantity()
        );

        assertEquals(
                0,
                sell1.getRemainingQuantity()
        );

        assertEquals(
                60,
                sell2.getRemainingQuantity()
        );
    }

    @Test
    void shouldCancelActiveOrder() {

        OrderBook orderBook =
                new OrderBook("AAPL");

        Order buy1 = new Order(
                1,
                "AAPL",
                OrderSide.BUY,
                18500,
                100
        );

        Order buy2 = new Order(
                2,
                "AAPL",
                OrderSide.BUY,
                18450,
                50
        );

        orderBook.addOrder(buy1);
        orderBook.addOrder(buy2);

        assertEquals(
                18500L,
                orderBook.getBestBid()
        );

        boolean cancelled =
                orderBook.cancelOrder(1);

        assertTrue(cancelled);

        assertEquals(
                OrderStatus.CANCELLED,
                buy1.getStatus()
        );

        assertFalse(
                orderBook.hasActiveOrder(1)
        );

        assertEquals(
                18450L,
                orderBook.getBestBid()
        );

        assertEquals(
                1,
                orderBook.getActiveOrderCount()
        );
    }

    @Test
    void shouldPartiallyFillLargerSellOrder() {

        OrderBook orderBook =
                new OrderBook("AAPL");

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
                100
        );

        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sellOrder);

        List<Trade> trades =
                orderBook.matchOrders();

        assertEquals(
                1,
                trades.size()
        );

        assertEquals(
                40,
                trades.get(0).getQuantity()
        );

        assertEquals(
                OrderStatus.FILLED,
                buyOrder.getStatus()
        );

        assertEquals(
                OrderStatus.PARTIALLY_FILLED,
                sellOrder.getStatus()
        );

        assertEquals(
                60,
                sellOrder.getRemainingQuantity()
        );

        assertTrue(
                orderBook.hasActiveOrder(2)
        );
    }

    @Test
    void shouldMatchOneBuyOrderAgainstMultipleSellOrders() {

        OrderBook orderBook =
                new OrderBook("AAPL");

        Order buyOrder = new Order(
                1,
                "AAPL",
                OrderSide.BUY,
                19000,
                200
        );

        Order sell1 = new Order(
                2,
                "AAPL",
                OrderSide.SELL,
                18500,
                50
        );

        Order sell2 = new Order(
                3,
                "AAPL",
                OrderSide.SELL,
                18600,
                70
        );

        Order sell3 = new Order(
                4,
                "AAPL",
                OrderSide.SELL,
                18700,
                100
        );

        orderBook.addOrder(buyOrder);
        orderBook.addOrder(sell1);
        orderBook.addOrder(sell2);
        orderBook.addOrder(sell3);

        List<Trade> trades =
                orderBook.matchOrders();

        assertEquals(
                3,
                trades.size()
        );

        assertEquals(
                50,
                trades.get(0).getQuantity()
        );

        assertEquals(
                19000L,
                trades.get(0).getPriceInCents()
        );

        assertEquals(
                70,
                trades.get(1).getQuantity()
        );

        assertEquals(
                19000L,
                trades.get(1).getPriceInCents()
        );

        assertEquals(
                80,
                trades.get(2).getQuantity()
        );

        assertEquals(
                19000L,
                trades.get(2).getPriceInCents()
        );

        assertEquals(
                OrderStatus.FILLED,
                buyOrder.getStatus()
        );

        assertEquals(
                0,
                buyOrder.getRemainingQuantity()
        );

        assertEquals(
                OrderStatus.PARTIALLY_FILLED,
                sell3.getStatus()
        );

        assertEquals(
                20,
                sell3.getRemainingQuantity()
        );

        assertEquals(
                3,
                orderBook.getTradeHistory().size()
        );
    }

    @Test
    void shouldRejectDuplicateActiveOrderId() {

        OrderBook orderBook =
                new OrderBook("AAPL");

        Order firstOrder = new Order(
                1,
                "AAPL",
                OrderSide.BUY,
                18500,
                100
        );

        Order duplicateOrder = new Order(
                1,
                "AAPL",
                OrderSide.SELL,
                19000,
                50
        );

        orderBook.addOrder(firstOrder);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderBook.addOrder(
                                duplicateOrder
                        )
                );

        assertEquals(
                "Duplicate order ID: 1",
                exception.getMessage()
        );

        assertEquals(
                1,
                orderBook.getActiveOrderCount()
        );
    }
    @Test
    void shouldExecuteAtRestingSellOrderPrice() {

        OrderBook orderBook =
                new OrderBook("AAPL");

        Order sellOrder = new Order(
                1,
                "AAPL",
                OrderSide.SELL,
                18500,
                100
        );

        Order buyOrder = new Order(
                2,
                "AAPL",
                OrderSide.BUY,
                19000,
                40
        );

        // SELL rests first
        orderBook.addOrder(sellOrder);

        // BUY arrives later
        orderBook.addOrder(buyOrder);

        List<Trade> trades =
                orderBook.matchOrders();

        assertEquals(
                1,
                trades.size()
        );

        assertEquals(
                18500L,
                trades.get(0).getPriceInCents()
        );
    }
    @Test
    void shouldAggregateBuyDepthByPriceLevel() {

        OrderBook orderBook =
                new OrderBook("AAPL");

        orderBook.addOrder(
                new Order(
                        1,
                        "AAPL",
                        OrderSide.BUY,
                        18900,
                        50
                )
        );

        orderBook.addOrder(
                new Order(
                        2,
                        "AAPL",
                        OrderSide.BUY,
                        18900,
                        30
                )
        );

        orderBook.addOrder(
                new Order(
                        3,
                        "AAPL",
                        OrderSide.BUY,
                        18800,
                        120
                )
        );

        List<PriceLevel> depth =
                orderBook.getBuyDepth();

        assertEquals(
                2,
                depth.size()
        );

        // Highest BUY price should come first
        assertEquals(
                18900L,
                depth.get(0).getPriceInCents()
        );

        assertEquals(
                80,
                depth.get(0).getTotalQuantity()
        );

        assertEquals(
                18800L,
                depth.get(1).getPriceInCents()
        );

        assertEquals(
                120,
                depth.get(1).getTotalQuantity()
        );
    }
    @Test
    void shouldAggregateSellDepthByPriceLevel() {

        OrderBook orderBook =
                new OrderBook("AAPL");

        orderBook.addOrder(
                new Order(
                        1,
                        "AAPL",
                        OrderSide.SELL,
                        19100,
                        50
                )
        );

        orderBook.addOrder(
                new Order(
                        2,
                        "AAPL",
                        OrderSide.SELL,
                        19000,
                        60
                )
        );

        orderBook.addOrder(
                new Order(
                        3,
                        "AAPL",
                        OrderSide.SELL,
                        19000,
                        40
                )
        );

        List<PriceLevel> depth =
                orderBook.getSellDepth();

        assertEquals(
                2,
                depth.size()
        );

        // Lowest SELL price should come first
        assertEquals(
                19000L,
                depth.get(0).getPriceInCents()
        );

        assertEquals(
                100,
                depth.get(0).getTotalQuantity()
        );

        assertEquals(
                19100L,
                depth.get(1).getPriceInCents()
        );

        assertEquals(
                50,
                depth.get(1).getTotalQuantity()
        );
    }
}