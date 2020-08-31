package dev.akif.exchange.conversion.impl;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.common.PagedResponse;
import dev.akif.exchange.common.PagingHelper;
import dev.akif.exchange.conversion.ConversionRepository;
import dev.akif.exchange.conversion.ConversionService;
import dev.akif.exchange.conversion.dto.ConversionResponse;
import dev.akif.exchange.conversion.model.Conversion;
import dev.akif.exchange.provider.TimeProvider;
import dev.akif.exchange.rate.RateService;
import e.java.E;
import e.java.EOr;

@Service
public class ConversionServiceImpl implements ConversionService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConversionRepository conversionRepository;
    private final RateService rateService;
    private final TimeProvider timeProvider;
    private final int defaultPageSize;

    @Autowired
    public ConversionServiceImpl(ConversionRepository conversionRepository,
                                 RateService rateService,
                                 TimeProvider timeProvider,
                                 @Value("${conversion.paging.defaultSize}") int defaultPageSize) {
        this.conversionRepository = conversionRepository;
        this.rateService          = rateService;
        this.timeProvider         = timeProvider;
        this.defaultPageSize      = defaultPageSize;
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
                                                            .cause(E.fromThrowable(t))
            ).map(
                ConversionResponse::new
            );
        });

        response.forEach(r -> logger.info("Converted, {} {} is {} {}", r.sourceAmount, r.source, r.targetAmount, r.target));

        return response;
    }

    @Override
    public EOr<ConversionResponse> get(long id) {
        logger.info("Getting conversion {}", id);

        return EOr.catching(
            () -> conversionRepository.findById(id),
            t  -> Errors.Conversion.cannotReadConversion.data("id", id).cause(E.fromThrowable(t))
        ).flatMap(maybeConversion ->
            EOr.fromOptional(
                maybeConversion,
                () -> Errors.Conversion.conversionNotFound.data("id", id)
            )
        ).map(
            ConversionResponse::new
        );
    }

    @Override
    public EOr<PagedResponse<ConversionResponse>> list(LocalDate fromDate, LocalDate toDate, int page, int size, boolean newestFirst) {
        long from = PagingHelper.from(fromDate);
        long to   = PagingHelper.to(toDate);

        PageRequest pageRequest = PageRequest.of(
            PagingHelper.page(page),
            PagingHelper.size(size,  defaultPageSize),
            Sort.by(newestFirst ? Order.desc("createdAt") : Order.asc("createdAt"))
        );

        return EOr.catching(
            () -> conversionRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(from, to, pageRequest),
            t  -> Errors.Conversion.cannotReadConversion.data("from", fromDate)
                                                        .data("to", toDate)
                                                        .data("page", page)
                                                        .data("size", size)
                                                        .data("newestFirst", newestFirst)
                                                        .cause(E.fromThrowable(t))
        ).map(conversionsPage ->
            new PagedResponse<>(
                conversionsPage.map(ConversionResponse::new).toList(),
                conversionsPage.getPageable().getPageNumber() + 1,
                conversionsPage.getTotalPages(),
                conversionsPage.getPageable().getPageSize(),
                conversionsPage.getTotalElements()
            )
        );
    }
}
