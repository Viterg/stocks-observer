package ru.viterg.proselyte.stocksobs.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class StocksClient {

    private final WebClient webClient; // RestClient

    public Mono<String> sendRequest(String ticker, String apikey) {
        return webClient.get()
                .uri("/quote/{ticker}?apikey={apikey}", ticker, apikey)
                .retrieve()
                .bodyToMono(String.class);
    }
}
