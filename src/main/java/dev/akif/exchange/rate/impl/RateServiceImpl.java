package dev.akif.exchange.rate.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.provider.RateProvider;
import dev.akif.exchange.provider.TimeProvider;
import dev.akif.exchange.provider.dto.RateProviderResponse;
import dev.akif.exchange.rate.RateRepository;
import dev.akif.exchange.rate.RateService;
import dev.akif.exchange.rate.dto.RateResponse;
import dev.akif.exchange.rate.model.Rate;
import e.java.E;
import e.java.EOr;

@Service
public class RateServiceImpl implements RateService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RateProvider rateProvider;
    private final RateRepository rateRepository;
    private final TimeProvider timeProvider;
    private final long rateFreshnessThresholdInMillis;

    @Autowired
    public RateServiceImpl(RateProvider rateProvider,
                           RateRepository rateRepository,
                           TimeProvider timeProvider,
                           @Value("${rate.freshnessThresholdInMillis}") long rateFreshnessThresholdInMillis) {
        this.rateProvider                   = rateProvider;
        this.rateRepository                 = rateRepository;
        this.timeProvider                   = timeProvider;
        this.rateFreshnessThresholdInMillis = rateFreshnessThresholdInMillis;
    }

    @Override
    public EOr<RateResponse> rate(CurrencyPair pair) {
        logger.info("Getting rate for {}", pair);

        EOr<RateResponse> response = pair.getSource().equals(pair.getTarget()) ? EOr.from(new RateResponse(pair, 1.0)) :
            rateOfBaseTo(pair.getTarget()).flatMap(targetRate ->
                rateOfBaseTo(pair.getSource()).map(sourceRate ->
                    new RateResponse(pair, targetRate.rate / sourceRate.rate)
                )
            );

        response.forEach(r -> logger.info("Rate for {} is {}", pair, r.rate));

        return response;
    }

    public EOr<RateResponse> rateOfBaseTo(String currency) {
        String base = rateProvider.baseCurrency();

        if (currency.equals(base)) {
            return EOr.from(new RateResponse(base, currency, 1.0));
        }

        CurrencyPair pair = new CurrencyPair(base, currency);

        EOr<Double> rate = getRateFromDB(pair).flatMap(maybeRate ->
            maybeRate.map(r ->
                EOr.from(r.getRate())
            ).orElseGet(() ->
                getAndSaveLatestRates().map(rateProviderResponse ->
                    rateProviderResponse.getRates().get(currency)
                )
            )
        );

        rate.forEach(r -> logger.debug("Rate for {} is {}", pair, r));

        return rate.map(r -> new RateResponse(base, currency, r));
    }

    public EOr<Optional<Rate>> getRateFromDB(CurrencyPair id) {
        logger.debug("Getting rate for {} from DB", id);

        return EOr.catching(
            () -> rateRepository.findById(id),
            t  -> Errors.Rate.cannotReadRate.data("source", id.getSource())
                                            .data("target", id.getTarget())
                                            .cause(E.fromThrowable(t))
        ).map(maybeRate -> {
            Optional<Rate> filtered = maybeRate.filter(r ->
                (timeProvider.now() - r.getUpdatedAt()) < rateFreshnessThresholdInMillis
            );

            if (maybeRate.isPresent() && filtered.isEmpty()) {
                logger.warn("Rate for {} is found as {} but it is expired", id, maybeRate.get());
            }

            return filtered;
        });
    }

    public EOr<RateProviderResponse> getAndSaveLatestRates() {
        return rateProvider.latestRates().map(ratesResponse -> {
            String source = ratesResponse.getSource();
            long now = timeProvider.now();

            ratesResponse.getRates()
                .entrySet()
                .stream()
                .filter(e -> !e.getKey().equals(source))
                .map(e -> new Rate(source, e.getKey(), e.getValue(), now))
                .forEach(rate -> {
                    logger.debug("Saving rate {} to DB", rate);

                    EOr.catching(
                        () -> rateRepository.save(rate),
                        t  -> {
                            logger.error("Cannot save rate {} to DB", rate, t);

                            return E.empty;
                        }
                    );
                });

            return ratesResponse;
        });
    }
}
