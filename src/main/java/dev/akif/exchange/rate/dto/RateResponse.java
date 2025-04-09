package dev.akif.exchange.rate.dto;

import dev.akif.exchange.common.CurrencyPair;

public record RateResponse(String source, String target, double rate) {
  public RateResponse(CurrencyPair pair, double rate) {
    this(pair.source(), pair.target(), rate);
  }
}
