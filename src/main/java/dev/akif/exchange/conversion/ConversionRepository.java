package dev.akif.exchange.conversion;

import dev.akif.exchange.conversion.model.Conversion;

public interface ConversionRepository {
    Conversion save(Conversion conversion);
}
