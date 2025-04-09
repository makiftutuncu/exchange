package dev.akif.exchange.rate;

import dev.akif.exchange.common.CurrencyPair;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateRepository extends CrudRepository<Rate, CurrencyPair> {}
