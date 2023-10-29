package ru.viterg.proselyte.stocksobs.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.viterg.proselyte.stocksobs.client.StocksClient;
import ru.viterg.proselyte.stocksobs.entity.StocksHistory;
import ru.viterg.proselyte.stocksobs.repository.StocksHistoryRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        ofRemoteTickers.flatMap(remoteTickers -> {
                    tickers.clear();
                    tickers.addAll(remoteTickers);
                    return Mono.just(tickers);
                })
                .subscribe();
    }

    @Scheduled(fixedRate = 2L, timeUnit = SECONDS)
    public void saveStocksForTickersInSequential() {
        log.debug("Saving stocks for tickers: {} -- START", tickers);
        long start = System.currentTimeMillis();
        for (String ticker : tickers) {
            client.getStock(ticker)
                    .flatMap(stock -> repository.save(new StocksHistory(ticker, stock)))
                    .subscribe(stocksHistory -> log.info("Stock saved {}", stocksHistory));
        }
        long end = System.currentTimeMillis() - start;
        log.info("All stocks were updated");
        log.debug("Saving stocks for tickers: {} -- END, executing time: {}ms", tickers, end);
    }

    public void saveStocksForTickersInParallel() {
        log.debug("Saving stocks for tickers: {} -- START", tickers);
        long start = System.currentTimeMillis();
        List<StocksHistory> histories = tickers.parallelStream()
                .map(ticker -> client.getStockWithTicker(ticker).toFuture())
                .map(CompletableFuture::join)
                .map(p -> new StocksHistory(p.getFirst(), p.getSecond()))
                .toList();
        repository.saveAll(histories).subscribe();
        long end = System.currentTimeMillis() - start;
        log.info("All stocks were updated");
        log.debug("Saving stocks for tickers: {} -- END, executing time: {}ms", tickers, end);
    }

    @Scheduled(fixedRate = 5L, timeUnit = SECONDS)
    public void printTop5() {
        repository.getMostExpensiveStocksForLastTime(5)
                .collectList()
                .doOnSuccess(sh -> log.info("\n --- Top-5 most expensive stocks at this moment ---\n{}",
                                            formatToTable(sh, StocksHistory::getStock)))
                .then(repository.getWithLargestPercentageChange(5).collectList())
                .subscribe(sh -> log.info("\n --- Top-5 most changed stocks at this moment ---\n{}",
                                          formatToTable(sh, StocksHistory::getChangePercentage)));
    }
}
