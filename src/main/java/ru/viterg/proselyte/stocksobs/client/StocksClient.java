package ru.viterg.proselyte.stocksobs.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class StocksClient {

    private static final String API_KEY_HEADER = "API-KEY";
    private final WebClient webClient;
    private final String apikey;

    public StocksClient(WebClient webClient, @Value("${application.client.apikey}") String apikey) {
        this.webClient = webClient;
        this.apikey = apikey;
    }

    public Mono<BigDecimal> getStock(String ticker) {
        return sendStocksRequest(ticker, apikey);
    }

    public Mono<Pair<String, BigDecimal>> getStockWithTicker(String ticker) {
        return sendStocksRequest(ticker, apikey)
                .map(stock -> Pair.of(ticker, stock));
    }

    private Mono<BigDecimal> sendStocksRequest(String ticker, String apikey) {
        return webClient.get()
                .uri("/stocks/{stock_code}/quote", ticker)
                .header(API_KEY_HEADER, apikey)
                .retrieve()
                .bodyToMono(BigDecimal.class);
    }

    public Mono<List<String>> getCompanies() {
        return sendCompaniesRequest(apikey);
    }

    private Mono<List<String>> sendCompaniesRequest(String apikey) {
        return webClient.get()
                .uri("/companies")
                .header(API_KEY_HEADER, apikey)
                .retrieve()
                .bodyToMono(List.class)
                .map(list -> (List<String>) list);
    }
}
