package dev.akif.exchange.rate;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.rate.dto.RateResponse;
import e.java.EOr;

public interface RateService {
    EOr<RateResponse> rate(CurrencyPair pair);
}
