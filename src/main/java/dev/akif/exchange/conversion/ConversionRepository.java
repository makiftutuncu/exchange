package dev.akif.exchange.conversion;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import dev.akif.exchange.conversion.model.Conversion;

public interface ConversionRepository {
    Conversion save(Conversion conversion);

    Optional<Conversion> findById(long id);

    List<Conversion> findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(long from, long to, Pageable page);
}
