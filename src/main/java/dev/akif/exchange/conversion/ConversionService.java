package dev.akif.exchange.conversion;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.conversion.dto.ConversionResponse;
import e.java.EOr;

public interface ConversionService {
    EOr<ConversionResponse> convert(CurrencyPair pair, double amount);
}
