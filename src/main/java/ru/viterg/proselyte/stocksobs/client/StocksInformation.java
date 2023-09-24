package ru.viterg.proselyte.stocksobs.client;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class StocksInformation {
    /*
    "symbol" : "AAPL",
    "name" : "Apple Inc.",
    "price" : 146.15000000,
    "changesPercentage" : 2.60000000,
    "change" : 3.70000000,
    "dayLow" : 142.96000000,
    "dayHigh" : 147.09970000,
    "yearHigh" : 150.00000000,
    "yearLow" : 89.14500000,
    "marketCap" : 2438892617728.00000000,
    "priceAvg50" : 135.25772000,
    "priceAvg200" : 130.42052000,
    "volume" : 96350036,
    "avgVolume" : 84504517,
    "exchange" : "NASDAQ",
    "open" : 143.46000000,
    "previousClose" : 142.45000000,
    "eps" : 4.44900000,
    "pe" : 32.85008000,
    "earningsAnnouncement" : "2021-07-27T20:00:00.000+0000",
    "sharesOutstanding" : 16687599163,
    "timestamp" : 1626873796
    */
    private String ticker;
    private BigDecimal price;
    private Instant givenAt;
}
