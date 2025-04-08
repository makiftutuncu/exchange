package dev.akif.exchange.common;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class PagingHelper {
  private PagingHelper() {}

  public static long from(LocalDate date) {
    return date == null ? 0L : date.atStartOfDay(ZoneOffset.UTC).toEpochSecond() * 1000;
  }

  public static long to(LocalDate date) {
    return date == null
        ? Long.MAX_VALUE
        : date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond() * 1000;
  }

  public static int page(int page) {
    return page <= 0 ? 0 : page - 1;
  }

  public static int size(int size, int defaultSize) {
    if (size <= 0 || size > 50) {
      return defaultSize;
    }
    return size;
  }
}
