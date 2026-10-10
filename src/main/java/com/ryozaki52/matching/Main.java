package com.ryozaki52.matching;

public class Main {

    public static void main(String[] args) {

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

        orderBook.addOrder(
                new Order(
                        4,
                        "AAPL",
                        OrderSide.SELL,
                        19000,
                        100
                )
        );

        orderBook.addOrder(
                new Order(
                        5,
                        "AAPL",
                        OrderSide.SELL,
                        19100,
                        50
                )
        );

        System.out.println("AAPL ORDER BOOK");
        System.out.println();

        System.out.println("SELL");

        for (PriceLevel level :
                orderBook.getSellDepth()) {

            System.out.println(
                    "$"
                            + level.getPriceAsDollars()
                            + "  "
                            + level.getTotalQuantity()
            );
        }

        System.out.println();
        System.out.println("----------------");
        System.out.println();

        for (PriceLevel level :
                orderBook.getBuyDepth()) {

            System.out.println(
                    "$"
                            + level.getPriceAsDollars()
                            + "  "
                            + level.getTotalQuantity()
            );
        }

        System.out.println("BUY");
    }
}