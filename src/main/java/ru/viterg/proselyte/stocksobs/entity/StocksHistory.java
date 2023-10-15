package ru.viterg.proselyte.stocksobs.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table(name = "stocks_history", schema = "public")
public class StocksHistory {

    @Id
    @Column("id")
    private UUID id;

    @Column("ticker")
    private String ticker;

    @Column("actual_on")
    private Instant actualOn;

    @Column("stock")
    private BigDecimal stock;

    @Column("change_percentage")
    private BigDecimal changePercentage;

    public StocksHistory(String ticker, BigDecimal stock) {
        this.ticker = ticker;
        this.stock = stock;
        actualOn = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        changePercentage = BigDecimal.ZERO;
    }
}
