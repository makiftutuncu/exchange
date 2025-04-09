package dev.akif.exchange.rate;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.fixerio.FixerIO;
import dev.akif.exchange.fixerio.FixerIOResponse;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final FixerIO fixerIO;
  private final RateRepository rateRepository;
  private final long rateFreshnessThresholdInMillis;
  private final String baseCurrency;

  @Autowired
  public RateService(
      FixerIO fixerIO,
      RateRepository rateRepository,
      @Value("${rate.freshnessThresholdInMillis}") long rateFreshnessThresholdInMillis,
      @Value("${provider.fixerio.baseCurrency}") String baseCurrency) {
    this.fixerIO = fixerIO;
    this.rateRepository = rateRepository;
    this.rateFreshnessThresholdInMillis = rateFreshnessThresholdInMillis;
    this.baseCurrency = baseCurrency;
  }

  public RateResponse rate(CurrencyPair pair) {
    logger.info("Getting rate for {}", pair);

    RateResponse response;
    if (pair.source().equals(pair.target())) {
      response = new RateResponse(pair, 1.0);
    } else {
      RateResponse targetRate = rateOfBaseTo(pair.target());
      RateResponse sourceRate = rateOfBaseTo(pair.source());
      response = new RateResponse(pair, targetRate.rate() / sourceRate.rate());
    }

    logger.info("Rate for {} is {}", pair, response.rate());

    return response;
  }

  public RateResponse rateOfBaseTo(String currency) {
    if (currency.equals(baseCurrency)) {
      return new RateResponse(baseCurrency, currency, 1.0);
    }

    CurrencyPair pair = new CurrencyPair(baseCurrency, currency);

    double rate =
        getRateFromDB(pair)
            .map(r -> r.getRate())
            .orElseGet(
                () -> {
                  FixerIOResponse fixerIOResponse = getAndSaveLatestRates();
                  return fixerIOResponse.rates().get(currency);
                });

    logger.debug("Rate for {} is {}", pair, rate);

    return new RateResponse(baseCurrency, currency, rate);
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
      throw Errors.Rate.cannotReadRate(e, id.source(), id.target());
    }
  }

  public FixerIOResponse getAndSaveLatestRates() {
    FixerIOResponse ratesResponse = fixerIO.latestRates();
    String source = ratesResponse.source();
    long now = System.currentTimeMillis();

    ratesResponse.rates().entrySet().stream()
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
