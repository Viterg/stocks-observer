package ru.viterg.proselyte.stocksobs.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.viterg.proselyte.stocksobs.client.StocksClient;
import ru.viterg.proselyte.stocksobs.entity.StocksHistory;
import ru.viterg.proselyte.stocksobs.repository.StocksHistoryRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.viterg.proselyte.stocksobs.service.LogFormatUtils.formatToTable;

@Slf4j
@Service
@RequiredArgsConstructor
public class StocksService {

    private final StocksClient client;
    private final StocksHistoryRepository repository;
    @Getter
    private final Collection<String> tickers = new HashSet<>(8000);

    @Scheduled(fixedDelay = 1L, timeUnit = DAYS)
    public void saveAllTickers() {
        Mono<List<String>> ofRemoteTickers = client.getCompanies();
        ofRemoteTickers.doOnSuccess(remoteTickers -> {
                    tickers.clear();
                    tickers.addAll(remoteTickers);
                })
                .subscribe();
    }

    public void saveStocksForTickersInParallel() {
        List<StocksHistory> histories = tickers.parallelStream()
                .map(ticker -> client.getStockWithTicker(ticker).toFuture())
                .map(CompletableFuture::join)
                .map(p -> new StocksHistory(p.getFirst(), p.getSecond()))
                .toList();
        repository.saveAll(histories).subscribe(stocksHistory -> log.info("All stocks were updated"));
    }

    public void saveStocksForTickersInParallel2() {
        Iterable<StocksHistory> histories = Flux.fromStream(tickers.parallelStream().map(client::getStockWithTicker))
                .flatMap(Function.identity())
                .map(p -> new StocksHistory(p.getFirst(), p.getSecond()))
                .toIterable();
        repository.saveAll(histories).subscribe(stocksHistory -> log.info("All stocks were updated"));
    }

    public void saveStocksForTickersInSequential() {
        for (String ticker : tickers) {
            client.getStock(ticker)
                    .flatMap(stock -> repository.save(new StocksHistory(ticker, stock)))
                    .subscribe(stocksHistory -> log.info("Stock saved {}", stocksHistory));
        }
    }

    @Scheduled(fixedRate = 5L, timeUnit = SECONDS)
    public void printTop5() {
        repository.getMostExpensiveStocksForLastTime(5)
                .collectList()
                .doOnSuccess(sh -> log.info("\n --- Top-5 most expensive stocks at this moment ---\n{}",
                                            formatToTable(sh, StocksHistory::getStock)))
                .then(repository.getMostExpensiveStocksForLastTime(5).collectList())
                .subscribe(sh -> log.info("\n --- Top-5 most changed stocks at this moment ---\n{}",
                                          formatToTable(sh, StocksHistory::getChangePercentage)));
    }
}
