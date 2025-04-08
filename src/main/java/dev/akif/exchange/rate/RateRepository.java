package dev.akif.exchange.rate;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.rate.model.Rate;
import java.util.Optional;

public interface RateRepository {
  Optional<Rate> findById(CurrencyPair id);

  Rate save(Rate rate);
}
