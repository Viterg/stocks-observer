package ru.viterg.proselyte.stocksobs.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.viterg.proselyte.stocksobs.entity.StocksHistory;
import ru.viterg.proselyte.stocksobs.service.StocksService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StocksRestControllerV1 {

    private final StocksService stocksService;

    @GetMapping("/companies")
    public Mono<List<String>> getCompanies() {
        return Mono.just(stocksService.getCompanies());
    }

    @GetMapping("/{ticker}/stock")
    public Mono<BigDecimal> getStock(@PathVariable String ticker) {
        return stocksService.getStock(ticker).map(StocksHistory::getStock);
    }
}
