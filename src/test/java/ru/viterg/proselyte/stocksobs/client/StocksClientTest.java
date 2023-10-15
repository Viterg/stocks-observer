package ru.viterg.proselyte.stocksobs.client;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@WireMockTest(httpPort = 8086)
class StocksClientTest {

    private final WebClient webClient = buildClient();
    private final StocksClient stocksClient = new StocksClient(webClient, "apikey");

    private static WebClient buildClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8086/api/v1/sec")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .option(CONNECT_TIMEOUT_MILLIS, 2000)
                                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(1000, MILLISECONDS)))
                                .responseTimeout(Duration.ofMillis(1000))))
                .build();
    }

    @Test
    @DisplayName("getCompanies() should return list of companies")
    void getCompanies() {
        stubFor(get(urlEqualTo("/api/v1/sec/companies"))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                            .withBody("[\"AAPL\", \"MSFT\", \"TSLA\"]")));

        List<String> companies = stocksClient.getCompanies().block();

        assertThat(companies).containsAll(List.of("AAPL", "MSFT", "TSLA"));
    }

    @Test
    @DisplayName("getStock() should return stock for ticker")
    void getStock() {
        stubFor(get(urlEqualTo("/api/v1/sec/stocks/AAPL/quote"))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                            .withBody("284.22")));

        BigDecimal stock = stocksClient.getStock("AAPL").block();

        assertThat(stock).isEqualTo(BigDecimal.valueOf(284.22));
    }

    @Test
    @DisplayName("getStock() should return stock for ticker")
    void getTickerStock() {
        stubFor(get(urlEqualTo("/api/v1/sec/stocks/AAPL/quote"))
                        .willReturn(aResponse()
                                            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                            .withBody("284.22")));

        Pair<String, BigDecimal> stock = stocksClient.getStockWithTicker("AAPL").block();

        assertThat(stock).isNotNull();
        assertThat(stock.getSecond()).isEqualTo(BigDecimal.valueOf(284.22));
    }
}