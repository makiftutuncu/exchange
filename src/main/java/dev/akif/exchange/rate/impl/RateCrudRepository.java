package dev.akif.exchange.rate.impl;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.rate.RateRepository;
import dev.akif.exchange.rate.model.Rate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateCrudRepository extends RateRepository, CrudRepository<Rate, CurrencyPair> {}
