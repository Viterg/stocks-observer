package ru.viterg.proselyte.stocksobs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stocks_history", schema = "public")
public class StocksHistory {

    @Id
    @Embedded.Empty
    private EmbeddedId embeddedId;

    @Column("stock")
    private BigDecimal stock;

    @Column("change_percentage")
    private int changePercentage;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    class EmbeddedId {

        @Column("ticker")
        private String ticker;

        @Column("actual_on")
        private Instant actualOn;
    }
}
