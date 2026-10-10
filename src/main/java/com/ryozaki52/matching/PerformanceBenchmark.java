package com.ryozaki52.matching;

import java.util.List;
import java.util.Random;

public class PerformanceBenchmark {

    private static final String[] SYMBOLS = {
            "AAPL",
            "TSLA",
            "NVDA",
            "MSFT"
    };

    private static final int RUNS = 5;

    public static void main(String[] args) {

        System.out.println(
                "Matching Engine Performance Benchmark"
        );

        System.out.println(
                "-------------------------------------"
        );

        System.out.println("Warming up JVM...");

        runSingleBenchmark(
                100_000,
                0
        );

        System.out.println("Warm-up complete.");

        benchmark(10_000);
        benchmark(50_000);
        benchmark(100_000);
        benchmark(500_000);
    }

    private static void benchmark(
            int numberOfOrders) {

        double totalMilliseconds = 0;
        double totalThroughput = 0;
        double totalMicroseconds = 0;
        long totalTrades = 0;

        System.out.println();
        System.out.println(
                "Orders: "
                        + String.format(
                        "%,d",
                        numberOfOrders
                )
        );

        for (int run = 1; run <= RUNS; run++) {

            BenchmarkResult result =
                    runSingleBenchmark(
                            numberOfOrders,
                            run
                    );

            totalMilliseconds +=
                    result.elapsedMilliseconds();

            totalThroughput +=
                    result.ordersPerSecond();

            totalMicroseconds +=
                    result.averageMicroseconds();

            totalTrades +=
                    result.tradesGenerated();

            System.out.printf(
                    "Run %d: %.2f ms | %,.0f orders/sec%n",
                    run,
                    result.elapsedMilliseconds(),
                    result.ordersPerSecond()
            );
        }

        System.out.println(
                "-------------------------------------"
        );

        System.out.printf(
                "Average Time: %.2f ms%n",
                totalMilliseconds / RUNS
        );

        System.out.printf(
                "Average Throughput: %,.0f orders/sec%n",
                totalThroughput / RUNS
        );

        System.out.printf(
                "Average Latency: %.3f microseconds/order%n",
                totalMicroseconds / RUNS
        );

        System.out.println(
                "Average Trades Generated: "
                        + String.format(
                        "%,d",
                        totalTrades / RUNS
                )
        );
    }

    private static BenchmarkResult runSingleBenchmark(
            int numberOfOrders,
            int runNumber) {

        MatchingEngine engine =
                new MatchingEngine();

        Random random =
                new Random(42);

        long totalTrades = 0;

        long startTime =
                System.nanoTime();

        for (int i = 1;
             i <= numberOfOrders;
             i++) {

            String symbol =
                    SYMBOLS[
                            random.nextInt(
                                    SYMBOLS.length
                            )
                            ];

            OrderSide side =
                    random.nextBoolean()
                            ? OrderSide.BUY
                            : OrderSide.SELL;

            long priceInCents =
                    9900
                            + random.nextInt(201);

            int quantity =
                    1
                            + random.nextInt(100);

            Order order =
                    new Order(
                            i,
                            symbol,
                            side,
                            priceInCents,
                            quantity
                    );

            List<Trade> trades =
                    engine.submitOrder(order);

            totalTrades +=
                    trades.size();
        }

        long elapsedNanoseconds =
                System.nanoTime()
                        - startTime;

        double elapsedMilliseconds =
                elapsedNanoseconds
                        / 1_000_000.0;

        double elapsedSeconds =
                elapsedNanoseconds
                        / 1_000_000_000.0;

        double ordersPerSecond =
                numberOfOrders
                        / elapsedSeconds;

        double averageMicroseconds =
                (elapsedNanoseconds / 1_000.0)
                        / numberOfOrders;

        return new BenchmarkResult(
                elapsedMilliseconds,
                ordersPerSecond,
                averageMicroseconds,
                totalTrades
        );
    }

    private record BenchmarkResult(
            double elapsedMilliseconds,
            double ordersPerSecond,
            double averageMicroseconds,
            long tradesGenerated) {
    }
}