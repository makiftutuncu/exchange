package dev.akif.exchange.common;

import java.time.LocalDate;
import java.time.ZoneOffset;

import e.java.EOr;

public class PagingHelper {
    private PagingHelper() {}

    public static long from(LocalDate date) {
        return date == null ? 0L : date.atStartOfDay(ZoneOffset.UTC).toEpochSecond() * 1000;
    }

    public static long to(LocalDate date) {
        return date == null ? Long.MAX_VALUE : date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond() * 1000;
    }

    public static int page(int page) {
        return EOr.from(page).filter(p -> p > 0).map(p -> p - 1).getOrElse(() -> 0);
    }

    public static int size(int size, int defaultSize) {
        return EOr.from(size).filter(s -> s > 0 && s <= 50).getOrElse(() -> defaultSize);
    }
}
