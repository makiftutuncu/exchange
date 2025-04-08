package dev.akif.exchange.provider;

import dev.akif.exchange.provider.dto.RateProviderResponse;

public interface RateProvider {
  String baseCurrency();

  RateProviderResponse latestRates();
}
