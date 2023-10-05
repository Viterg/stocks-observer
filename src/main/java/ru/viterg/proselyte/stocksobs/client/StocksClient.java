package ru.viterg.proselyte.stocksobs.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StocksClient {

    private static final String API_KEY_HEADER = "API-KEY";
    private final WebClient webClient;

    @Value("${application.client.apikey}")
    private String apikey;

    public Mono<BigDecimal> getStock(String ticker) {
        return sendStocksRequest(ticker, apikey);
    }

    private Mono<BigDecimal> sendStocksRequest(String ticker, String apikey) {
        return webClient.get()
                .uri("/stocks/{stock_code}/quote", ticker)
                .header(API_KEY_HEADER, apikey)
                .retrieve()
                .bodyToMono(BigDecimal.class);
    }

    public Mono<List> getCompanies() {
        return sendCompaniesRequest(apikey);
    }

    private Mono<List> sendCompaniesRequest(String apikey) {
        return webClient.get()
                .uri("/companies")
                .header(API_KEY_HEADER, apikey)
                .retrieve()
                .bodyToMono(List.class);
    }
}
