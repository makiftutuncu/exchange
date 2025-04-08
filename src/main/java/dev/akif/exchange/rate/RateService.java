package dev.akif.exchange.rate;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.rate.dto.RateResponse;

public interface RateService {
  RateResponse rate(CurrencyPair pair);
}
