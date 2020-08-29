package dev.akif.exchange.rate;

import java.util.Optional;

import dev.akif.exchange.rate.model.Rate;
import dev.akif.exchange.rate.model.RateId;

public interface RateRepository {
    Optional<Rate> findById(RateId id);

    Rate save(Rate rate);
}
