package dev.akif.exchange.provider;

import dev.akif.exchange.provider.dto.RateProviderResponse;
import e.java.EOr;

public interface RateProvider {
    String baseCurrency();

    EOr<RateProviderResponse> latestRates();
}
