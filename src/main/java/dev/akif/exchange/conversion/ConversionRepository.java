package dev.akif.exchange.conversion;

import dev.akif.exchange.conversion.model.Conversion;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConversionRepository {
  Conversion save(Conversion conversion);

  Optional<Conversion> findById(long id);

  Page<Conversion> findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
      long from, long to, Pageable page);
}
