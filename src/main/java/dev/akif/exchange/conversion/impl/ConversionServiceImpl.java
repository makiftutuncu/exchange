package dev.akif.exchange.conversion.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.conversion.ConversionRepository;
import dev.akif.exchange.conversion.ConversionService;
import dev.akif.exchange.conversion.dto.ConversionResponse;
import dev.akif.exchange.conversion.model.Conversion;
import dev.akif.exchange.provider.TimeProvider;
import dev.akif.exchange.rate.RateService;
import e.java.EOr;

@Service
public class ConversionServiceImpl implements ConversionService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConversionRepository conversionRepository;
    private final RateService rateService;
    private final TimeProvider timeProvider;

    @Autowired
    public ConversionServiceImpl(ConversionRepository conversionRepository,
                                 RateService rateService,
                                 TimeProvider timeProvider) {
        this.conversionRepository = conversionRepository;
        this.rateService          = rateService;
        this.timeProvider         = timeProvider;
    }

    @Override
    public EOr<ConversionResponse> convert(CurrencyPair pair, double amount) {
        logger.info("Converting {} {} to {}", amount, pair.getSource(), pair.getTarget());

        EOr<ConversionResponse> response = rateService.rate(pair).flatMap(rate -> {
            double targetAmount = amount * rate.rate;
            long now = timeProvider.now();

            Conversion conversion = new Conversion(pair, rate.rate, amount, targetAmount, now);

            logger.debug("Saving conversion {}", conversion);

            return EOr.catching(
                () -> conversionRepository.save(conversion),
                t  -> Errors.Conversion.cannotSaveConversion.data("source", pair.getSource())
                                                            .data("target", pair.getTarget())
                                                            .data("amount", amount)
            ).map(
                ConversionResponse::new
            );
        });

        response.forEach(r -> logger.info("Converted, {} {} is {} {}", r.sourceAmount, r.source, r.targetAmount, r.target));

        return response;
    }
}
