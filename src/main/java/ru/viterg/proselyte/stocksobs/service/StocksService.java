package ru.viterg.proselyte.stocksobs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.viterg.proselyte.stocksobs.client.StocksClient;
import ru.viterg.proselyte.stocksobs.repository.StocksHistoryRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class StocksService {

    private final StocksClient client;
    private final StocksHistoryRepository repository;
    private final Collection<String> tickers = new HashSet<>(8000);

    @Scheduled(fixedDelay = 1L, timeUnit = TimeUnit.DAYS)
    public void saveAllTickers() {
        Mono<List> companies = client.getCompanies();
        companies.doOnSuccess(list -> {
                    tickers.clear();
                    tickers.addAll(list);
                })
                .subscribe();
    }

    // get and save stock for every ticker one-by-one, after that do it a
    public void getStocksForTickers() {
        /*
        - пройти по каждой компании и поместить запрос в очередь для получения данных о ее акциях.
        - сделать несколько потоков для обработки очереди, которые будут загружать текущую информацию о ценах акций для каждой компании
        - сохранить данные из каждого запроса об акциях в БД
        - после прохода по всем компаниям начать заново. Если информация изменилась для компании, то записать в БД
        */
    }

    @Scheduled(fixedRate = 5L, timeUnit = TimeUnit.SECONDS)
    public void printTop5() {
        List top5Expensive = getTop5Expensive();
        List top5MostChanged = getTop5MostChanged();
        /* - Топ 5 акций с наивысшей стоимостью (сортировка: сначала наибольшая стоимость, затем по имени компании).
           - Последние 5 компаний с наибольшим процентным изменением стоимости акций */
        log.info("Top-5 at this moment: ");
    }

    private List getTop5Expensive() {
        return null;
    }

    private List getTop5MostChanged() {
        return null;
    }

}
