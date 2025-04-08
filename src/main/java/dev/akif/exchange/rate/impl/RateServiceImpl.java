package dev.akif.exchange.rate.impl;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.provider.RateProvider;
import dev.akif.exchange.provider.dto.RateProviderResponse;
import dev.akif.exchange.rate.RateRepository;
import dev.akif.exchange.rate.RateService;
import dev.akif.exchange.rate.dto.RateResponse;
import dev.akif.exchange.rate.model.Rate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateServiceImpl implements RateService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RateProvider rateProvider;
  private final RateRepository rateRepository;
  private final long rateFreshnessThresholdInMillis;

  @Autowired
  public RateServiceImpl(
      RateProvider rateProvider,
      RateRepository rateRepository,
      @Value("${rate.freshnessThresholdInMillis}") long rateFreshnessThresholdInMillis) {
    this.rateProvider = rateProvider;
    this.rateRepository = rateRepository;
    this.rateFreshnessThresholdInMillis = rateFreshnessThresholdInMillis;
  }

  @Override
  public RateResponse rate(CurrencyPair pair) {
    logger.info("Getting rate for {}", pair);

    RateResponse response;
    if (pair.getSource().equals(pair.getTarget())) {
      response = new RateResponse(pair, 1.0);
    } else {
      RateResponse targetRate = rateOfBaseTo(pair.getTarget());
      RateResponse sourceRate = rateOfBaseTo(pair.getSource());
      response = new RateResponse(pair, targetRate.rate / sourceRate.rate);
    }

    logger.info("Rate for {} is {}", pair, response.rate);

    return response;
  }

  public RateResponse rateOfBaseTo(String currency) {
    String base = rateProvider.baseCurrency();

    if (currency.equals(base)) {
      return new RateResponse(base, currency, 1.0);
    }

    CurrencyPair pair = new CurrencyPair(base, currency);

    double rate =
        getRateFromDB(pair)
            .map(r -> r.getRate())
            .orElseGet(
                () -> {
                  RateProviderResponse rateProviderResponse = getAndSaveLatestRates();
                  return rateProviderResponse.getRates().get(currency);
                });

    logger.debug("Rate for {} is {}", pair, rate);

    return new RateResponse(base, currency, rate);
  }

  public Optional<Rate> getRateFromDB(CurrencyPair id) {
    logger.debug("Getting rate for {} from DB", id);

    try {
      Optional<Rate> maybeRate = rateRepository.findById(id);
      Optional<Rate> filtered =
          maybeRate.filter(
              r ->
                  (System.currentTimeMillis() - r.getUpdatedAt()) < rateFreshnessThresholdInMillis);

      if (maybeRate.isPresent() && filtered.isEmpty()) {
        logger.warn("Rate for {} is found as {} but it is expired", id, maybeRate.get());
      }

      return filtered;
    } catch (Exception e) {
      throw Errors.Rate.cannotReadRate(e, id.getSource(), id.getTarget());
    }
  }

  public RateProviderResponse getAndSaveLatestRates() {
    RateProviderResponse ratesResponse = rateProvider.latestRates();
    String source = ratesResponse.getSource();
    long now = System.currentTimeMillis();

    ratesResponse.getRates().entrySet().stream()
        .filter(e -> !e.getKey().equals(source))
        .map(e -> new Rate(source, e.getKey(), e.getValue(), now))
        .forEach(
            rate -> {
              logger.debug("Saving rate {} to DB", rate);

              try {
                rateRepository.save(rate);
              } catch (Exception e) {
                logger.error("Cannot save rate {} to DB", rate, e);
              }
            });

    return ratesResponse;
  }
}
