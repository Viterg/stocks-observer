package ru.viterg.proselyte.stocksobs.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.test.StepVerifier;
import ru.viterg.proselyte.stocksobs.client.StocksClient;
import ru.viterg.proselyte.stocksobs.entity.StocksHistory;
import ru.viterg.proselyte.stocksobs.repository.StocksHistoryRepository;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@WireMockTest(httpPort = 8086)
@SpringBootTest(classes = ServiceTestConfiguration.class)
class StocksServiceIT {

    @Autowired
    private StocksService stocksService;
    @Autowired
    private StocksClient stocksClient;
    @Autowired
    private StocksHistoryRepository stocksHistoryRepository;
    @Autowired
    private PostgreSQLContainer<?> postgreSQLContainer;

    @BeforeEach
    void setUp() {
        stocksService.getTickers().clear();
    }

    @Test
    void testScriptExecution() {
        String logs = postgreSQLContainer.getLogs();
        assertThat(logs.contains("Executing database script from")).isTrue();
    }

    @Test
    void saveAllTickers() {
        stubFor(get(urlEqualTo("/api/v1/sec/companies"))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                            .withBody("[\"AAPL\", \"MSFT\", \"TSLA\"]")));

        stocksService.saveAllTickers();

        StepVerifier.create(stocksClient.getCompanies())
                .expectNext(List.of("AAPL", "MSFT", "TSLA"))
                .verifyComplete();
    }

    @Test
    void saveStocksForTickersInParallel() {
        stocksService.getTickers().addAll(List.of("AAPL", "MSFT", "TSLA"));

        stubFor(get(urlEqualTo("/api/v1/sec/stocks/AAPL/quote"))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                            .withBody("284.22")));

        stubFor(get(urlEqualTo("/api/v1/sec/stocks/MSFT/quote"))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                            .withBody("144.29")));

        stubFor(get(urlEqualTo("/api/v1/sec/stocks/TSLA/quote"))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                            .withBody("98.631")));

        stocksService.saveStocksForTickersInParallel();

        List<StocksHistory> stocks = stocksHistoryRepository.findAll().collectList().block();
        assertThat(stocks).hasSize(3);
        assertThat(stocks.get(0).getTicker()).isEqualTo("AAPL");
        assertThat(stocks.get(0).getStock()).isEqualTo(BigDecimal.valueOf(284.22));
        assertThat(stocks.get(1).getTicker()).isEqualTo("MSFT");
        assertThat(stocks.get(1).getStock()).isEqualTo(BigDecimal.valueOf(144.29));
        assertThat(stocks.get(2).getTicker()).isEqualTo("TSLA");
        assertThat(stocks.get(2).getStock()).isEqualTo(BigDecimal.valueOf(98.631));
    }

    @Test
    void saveStocksForTickersInSequential() {
        stocksService.getTickers().addAll(List.of("AAPL", "MSFT", "TSLA"));

        stubFor(get(urlEqualTo("/api/v1/sec/stocks/AAPL/quote"))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                            .withBody("284.22")));

        stubFor(get(urlEqualTo("/api/v1/sec/stocks/MSFT/quote"))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                            .withBody("144.29")));

        stubFor(get(urlEqualTo("/api/v1/sec/stocks/TSLA/quote"))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                            .withBody("98.631")));

        stocksService.saveStocksForTickersInSequential();

        List<StocksHistory> stocks = stocksHistoryRepository.findAll().collectList().block();
        assertThat(stocks).hasSize(3);
        assertThat(stocks.get(0).getTicker()).isEqualTo("AAPL");
        assertThat(stocks.get(0).getStock()).isEqualTo(BigDecimal.valueOf(284.22));
        assertThat(stocks.get(1).getTicker()).isEqualTo("MSFT");
        assertThat(stocks.get(1).getStock()).isEqualTo(BigDecimal.valueOf(144.29));
        assertThat(stocks.get(2).getTicker()).isEqualTo("TSLA");
        assertThat(stocks.get(2).getStock()).isEqualTo(BigDecimal.valueOf(98.631));
    }

    @Test
    void printTop5() {
        StocksHistory sh1 = new StocksHistory("AAPL", BigDecimal.valueOf(284.22));
        sh1.setChangePercentage(BigDecimal.valueOf(0.8));
        StocksHistory sh2 = new StocksHistory("MSFT", BigDecimal.valueOf(144.29));
        sh2.setChangePercentage(BigDecimal.valueOf(2.2));
        StocksHistory sh3 = new StocksHistory("TSLA", BigDecimal.valueOf(98.631));
        sh3.setChangePercentage(BigDecimal.valueOf(-1.4));

        stocksHistoryRepository.saveAll(List.of(sh1, sh2, sh3)).blockLast();

        List<StocksHistory> expensiveStocks = stocksHistoryRepository.getMostExpensiveStocksForLastTime(5)
                .collectList().block();
        List<StocksHistory> maxChangedStocks = stocksHistoryRepository.getWithLargestPercentageChange(5)
                .collectList().block();

        stocksService.printTop5();

        verify(stocksHistoryRepository).getMostExpensiveStocksForLastTime(5);
        verify(stocksHistoryRepository).getWithLargestPercentageChange(5);

        assertThat(expensiveStocks).containsExactly(sh1, sh2, sh3);
        assertThat(maxChangedStocks).containsExactly(sh1, sh2, sh3);
    }
}