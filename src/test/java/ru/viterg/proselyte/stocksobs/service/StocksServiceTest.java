package ru.viterg.proselyte.stocksobs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.viterg.proselyte.stocksobs.client.StocksClient;
import ru.viterg.proselyte.stocksobs.entity.StocksHistory;
import ru.viterg.proselyte.stocksobs.repository.StocksHistoryRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StocksServiceTest {

    @InjectMocks
    private StocksService stocksService;
    @Mock
    private StocksClient client;
    @Mock
    private StocksHistoryRepository repository;

    @BeforeEach
    void setUp() {
        stocksService.getTickers().clear();
    }

    @Test
    @DisplayName("should get and save list of all tickers")
    void saveAllTickers() {
        when(client.getCompanies()).thenReturn(Mono.just(List.of("AAPL", "MSFT", "TSLA")));

        stocksService.saveAllTickers();

        verify(client).getCompanies();
        assertThat(stocksService.getTickers()).containsAll(List.of("AAPL", "MSFT", "TSLA"));
    }

    @Test
    @DisplayName("should save stocks for tickers in parallel")
    void saveStocksForTickersInParallel() {
        StocksHistory sh1 = new StocksHistory("AAPL", BigDecimal.valueOf(284.22));
        StocksHistory sh2 = new StocksHistory("MSFT", BigDecimal.valueOf(144.29));
        StocksHistory sh3 = new StocksHistory("TSLA", BigDecimal.valueOf(98.631));
        stocksService.getTickers().addAll(List.of("AAPL", "MSFT", "TSLA"));

        when(client.getStockWithTicker("AAPL")).thenReturn(Mono.just(Pair.of("AAPL", BigDecimal.valueOf(284.22))));
        when(client.getStockWithTicker("MSFT")).thenReturn(Mono.just(Pair.of("MSFT", BigDecimal.valueOf(144.29))));
        when(client.getStockWithTicker("TSLA")).thenReturn(Mono.just(Pair.of("TSLA", BigDecimal.valueOf(98.631))));
        when(repository.saveAll((Iterable<StocksHistory>) any())).thenReturn(Flux.just(sh1, sh2, sh3));

        stocksService.saveStocksForTickersInParallel();

        verify(client).getStockWithTicker("AAPL");
        verify(client).getStockWithTicker("MSFT");
        verify(client).getStockWithTicker("TSLA");
        verify(repository).saveAll((Iterable<StocksHistory>) any());
    }

    @Test
    @DisplayName("should save stocks for tickers in sequential")
    void saveStocksForTickersInSequential() {
        StocksHistory sh1 = new StocksHistory("AAPL", BigDecimal.valueOf(284.22));
        StocksHistory sh2 = new StocksHistory("MSFT", BigDecimal.valueOf(144.29));
        StocksHistory sh3 = new StocksHistory("TSLA", BigDecimal.valueOf(98.631));
        stocksService.getTickers().addAll(List.of("AAPL", "MSFT", "TSLA"));

        when(client.getStockWithTicker("AAPL")).thenReturn(Mono.just(Pair.of("AAPL", BigDecimal.valueOf(284.22))));
        when(client.getStockWithTicker("MSFT")).thenReturn(Mono.just(Pair.of("MSFT", BigDecimal.valueOf(144.29))));
        when(client.getStockWithTicker("TSLA")).thenReturn(Mono.just(Pair.of("TSLA", BigDecimal.valueOf(98.631))));
        when(repository.saveAll((Iterable<StocksHistory>) any())).thenReturn(Flux.just(sh1, sh2, sh3));

        stocksService.saveStocksForTickersInSequential();

        verify(client).getStockWithTicker("AAPL");
        verify(client).getStockWithTicker("MSFT");
        verify(client).getStockWithTicker("TSLA");
        verify(repository).saveAll((Iterable<StocksHistory>) any());
    }

    @Test
    @DisplayName("should print top 5")
    void printTop5() {
        StocksHistory sh1 = new StocksHistory("AAPL", BigDecimal.valueOf(284.22));
        sh1.setChangePercentage(BigDecimal.valueOf(0.8));
        StocksHistory sh2 = new StocksHistory("MSFT", BigDecimal.valueOf(144.29));
        sh2.setChangePercentage(BigDecimal.valueOf(2.2));
        StocksHistory sh3 = new StocksHistory("TSLA", BigDecimal.valueOf(98.631));
        sh3.setChangePercentage(BigDecimal.valueOf(-1.4));

        when(repository.getMostExpensiveStocksForLastTime(5)).thenReturn(Flux.just(sh1, sh2, sh3));
        when(repository.getWithLargestPercentageChange(5)).thenReturn(Flux.just(sh1, sh2, sh3));

        stocksService.printTop5();

        verify(repository).getMostExpensiveStocksForLastTime(5);
        verify(repository).getWithLargestPercentageChange(5);
    }
}