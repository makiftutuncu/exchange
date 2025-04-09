package dev.akif.exchange.conversion;

import dev.akif.exchange.common.CurrencyPair;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversionRepository extends CrudRepository<Conversion, CurrencyPair> {
  Optional<Conversion> findById(long id);

  Page<Conversion> findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
      long from, long to, Pageable page);
}
