package ru.viterg.proselyte.stocksobs.service;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import reactor.netty.http.client.HttpClient;
import ru.viterg.proselyte.stocksobs.client.StocksClient;
import ru.viterg.proselyte.stocksobs.repository.StocksHistoryRepository;

import java.time.Duration;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
@EnableR2dbcRepositories(basePackages = "ru.viterg.proselyte.stocksobs.repository")
public class ServiceTestConfiguration {

    @Value("${spring.datasource.container-name}")
    private String containerName;
    @Value("${spring.r2dbc.username}")
    private String username;
    @Value("${spring.r2dbc.password}")
    private String password;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public PostgreSQLR2DBCDatabaseContainer postgreSQLContainerR2dbc(PostgreSQLContainer postgreSQLContainer) {
        return new PostgreSQLR2DBCDatabaseContainer(postgreSQLContainer);
    }

    @Bean
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(containerName)
                .withUsername(username)
                .withPassword(password)
                .withInitScript("db/init_stocks_history.sql")
                .waitingFor(Wait.forListeningPort());
    }

    @Bean
    public ConnectionFactory connectionFactory(PostgreSQLContainer postgreSQLContainer) {
        return ConnectionFactories.get(String.format("r2dbc:postgresql://%s:%s@%s:%s/postgres",
                                                     username,
                                                     password,
                                                     postgreSQLContainer.getHost(),
                                                     postgreSQLContainer.getFirstMappedPort()));
    }

    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory connectionFactory) {
        return new R2dbcEntityTemplate(connectionFactory);
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

