package ru.viterg.proselyte.stocksobs.service;

import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import reactor.netty.http.client.HttpClient;
import ru.viterg.proselyte.stocksobs.client.StocksClient;
import ru.viterg.proselyte.stocksobs.repository.StocksHistoryRepository;

import java.time.Duration;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
@EnableR2dbcRepositories
public class ServiceTestConfiguration {

    private static PostgreSQLContainer<?> postgreSQLContainer;

    @Value("${spring.datasource.container-name}")
    private String containerName;
    @Value("${spring.r2dbc.username}")
    private String username;
    @Value("${spring.r2dbc.password}")
    private String password;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public JdbcDatabaseContainer<?> postgreSQLContainer() {
        if (postgreSQLContainer == null) {
            postgreSQLContainer = new PostgreSQLContainer<>(containerName)
                    .withUsername(username)
                    .withPassword(password)
                    .waitingFor(Wait.forListeningPort());
        }
        return postgreSQLContainer;
    }

    @Bean
    public StocksService stocksService(StocksClient stocksClient, StocksHistoryRepository repository) {
        return new StocksService(stocksClient, repository);
    }

    @Bean
    public StocksClient stocksClient(WebClient webClient, @Value("${application.client.apikey}") String apikey) {
        return new StocksClient(webClient, apikey);
    }

    @Bean
    public WebClient webClient(@Value("${application.client.sourceUrl}") String url) {
        return WebClient.builder()
                .baseUrl(url)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .option(CONNECT_TIMEOUT_MILLIS, 2000)
                                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(1000, MILLISECONDS)))
                                .responseTimeout(Duration.ofMillis(1000))))
                .build();
    }
}

