package dev.akif.exchange.rate;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.akif.exchange.common.Errors;
import dev.akif.exchange.provider.RateProvider;
import dev.akif.exchange.rate.dto.RateRequest;
import dev.akif.exchange.rate.dto.RateResponse;
import e.java.EOr;

@Service
public class RateServiceImpl implements RateService {
    private static final Set<String> supportedBaseCurrencies = Set.of("EUR");

    private final RateProvider rateProvider;

    @Autowired
    public RateServiceImpl(RateProvider rateProvider) {
        this.rateProvider = rateProvider;
    }

    @Override
    public EOr<RateResponse> rates(RateRequest request) {
        if (!supportedBaseCurrencies.contains(request.source)) {
            return Errors.Rate.currencyNotSupported.data("source", request.source).toEOr();
        }

        return rateProvider.rates().flatMap(rates ->
            EOr.fromNullable(
                rates.get(request.target),
                () -> Errors.Rate.notFound.data("target", request.target)
            ).map(rate ->
                new RateResponse(request.source, request.target, rate)
            )
        );
    }
}
