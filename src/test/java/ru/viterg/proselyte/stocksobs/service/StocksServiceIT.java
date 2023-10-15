package ru.viterg.proselyte.stocksobs.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@WireMockTest(httpPort = 8086)
@SpringBootTest(classes = ServiceTestConfiguration.class)
class StocksServiceIT {

    @Test
    void saveAllTickers() {
    }

    @Test
    void saveStocksForTickersInParallel() {
    }

    @Test
    void saveStocksForTickersInParallel2() {
    }

    @Test
    void saveStocksForTickersInSequential() {
    }

    @Test
    void printTop5() {
    }
}