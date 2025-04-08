package dev.akif.exchange.conversion;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.PagedResponse;
import dev.akif.exchange.conversion.dto.ConversionResponse;
import java.time.LocalDate;

public interface ConversionService {
  ConversionResponse convert(CurrencyPair pair, double amount);

  ConversionResponse get(long id);

  PagedResponse<ConversionResponse> list(
      LocalDate fromDate, LocalDate toDate, int page, int size, boolean newestFirst);
}
