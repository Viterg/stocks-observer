package ru.viterg.proselyte.stocksobs.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.viterg.proselyte.stocksobs.entity.StocksHistory;

@Repository
public interface StocksHistoryRepository extends R2dbcRepository<StocksHistory, String> {

    /**
     * Retrieves the top stocks with the highest value.
     * Sorting is done first by the highest value and then by the company name.
     *
     * @param limit the maximum number of records to retrieve.
     * @return a Flux of StocksHistory objects.
     */
    @Query("""
           SELECT * FROM (
               SELECT DISTINCT ON (ticker) * FROM stocks_history ORDER BY ticker, actual_on DESC) AS ul
           ORDER BY stock DESC, ticker ASC LIMIT :limit
           """)
    Flux<StocksHistory> getMostExpensiveStocksForLastTime(int limit);

    /**
     * Retrieves the latest stocks history records with the largest percentage change in stock price.
     *
     * @param limit the maximum number of records to retrieve.
     * @return a Flux of StocksHistory objects.
     */
    @Query("""
           SELECT * FROM (
               SELECT DISTINCT ON (ticker) * FROM stocks_history ORDER BY ticker, actual_on DESC) AS latest
           ORDER BY change_percentage DESC LIMIT :limit;
           """)
    Flux<StocksHistory> getWithLargestPercentageChange(int limit);

}
