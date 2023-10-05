package ru.viterg.proselyte.stocksobs.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.viterg.proselyte.stocksobs.entity.StocksHistory;

@Repository
public interface StocksHistoryRepository extends R2dbcRepository<StocksHistory, String> {
}
