package dev.akif.exchange.rate.impl;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.rate.RateRepository;
import dev.akif.exchange.rate.model.Rate;

@Repository
public interface RateCrudRepository extends RateRepository, CrudRepository<Rate, CurrencyPair> {}
