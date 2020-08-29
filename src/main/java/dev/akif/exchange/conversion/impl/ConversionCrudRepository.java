package dev.akif.exchange.conversion.impl;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.conversion.ConversionRepository;
import dev.akif.exchange.conversion.model.Conversion;

@Repository
public interface ConversionCrudRepository extends ConversionRepository, CrudRepository<Conversion, CurrencyPair> {}
