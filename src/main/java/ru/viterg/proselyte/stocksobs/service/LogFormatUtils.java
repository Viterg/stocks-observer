package ru.viterg.proselyte.stocksobs.service;

import ru.viterg.proselyte.stocksobs.entity.StocksHistory;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

class LogFormatUtils {
    static String formatToTable(List<StocksHistory> stocksHistories, Function<StocksHistory, BigDecimal> getter) {
        StringBuilder sb = new StringBuilder(16 * stocksHistories.size());
        for (StocksHistory stocksHistory : stocksHistories) {
            sb.append(stocksHistory.getTicker())
                    .append(" : ")
                    .append(getter.apply(stocksHistory))
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }
}
