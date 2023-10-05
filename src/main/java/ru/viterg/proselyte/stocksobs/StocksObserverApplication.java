package ru.viterg.proselyte.stocksobs;

import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@EnableScheduling
@SpringBootApplication
public class StocksObserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(StocksObserverApplication.class, args);
    }

    @Bean
    public WebClient webClient(HttpClient httpClient, @Value("${application.client.sourceUrl}") String url) {
        return WebClient.builder()
                .baseUrl(url)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.create()
                .option(CONNECT_TIMEOUT_MILLIS, 2000)
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(1000, MILLISECONDS)))
                .responseTimeout(Duration.ofMillis(1000));
    }
}
