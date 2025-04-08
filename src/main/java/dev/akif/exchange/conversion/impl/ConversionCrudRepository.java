package dev.akif.exchange.conversion.impl;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.conversion.ConversionRepository;
import dev.akif.exchange.conversion.model.Conversion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversionCrudRepository
    extends ConversionRepository, CrudRepository<Conversion, CurrencyPair> {}
