package dev.akif.exchange.rate;

import java.util.Optional;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.rate.model.Rate;

public interface RateRepository {
    Optional<Rate> findById(CurrencyPair id);

    Rate save(Rate rate);
}
