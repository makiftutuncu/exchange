package dev.akif.exchange.conversion;

import java.time.LocalDate;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.PagedResponse;
import dev.akif.exchange.conversion.dto.ConversionResponse;
import e.java.EOr;

public interface ConversionService {
    EOr<ConversionResponse> convert(CurrencyPair pair, double amount);

    EOr<ConversionResponse> get(long id);

    EOr<PagedResponse<ConversionResponse>> list(LocalDate fromDate, LocalDate toDate, int page, int size, boolean newestFirst);
}
