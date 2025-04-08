package dev.akif.exchange.conversion.impl;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.common.PagedResponse;
import dev.akif.exchange.common.PagingHelper;
import dev.akif.exchange.conversion.ConversionRepository;
import dev.akif.exchange.conversion.ConversionService;
import dev.akif.exchange.conversion.dto.ConversionResponse;
import dev.akif.exchange.conversion.model.Conversion;
import dev.akif.exchange.rate.RateService;
import dev.akif.exchange.rate.dto.RateResponse;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

@Service
public class ConversionServiceImpl implements ConversionService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ConversionRepository conversionRepository;
  private final RateService rateService;
  private final int defaultPageSize;

  @Autowired
  public ConversionServiceImpl(
      ConversionRepository conversionRepository,
      RateService rateService,
      @Value("${conversion.paging.defaultSize}") int defaultPageSize) {
    this.conversionRepository = conversionRepository;
    this.rateService = rateService;
    this.defaultPageSize = defaultPageSize;
  }

  @Override
  public ConversionResponse convert(CurrencyPair pair, double amount) {
    logger.info("Converting {} {} to {}", amount, pair.getSource(), pair.getTarget());

    RateResponse rate = rateService.rate(pair);
    double targetAmount = amount * rate.rate;
    long now = System.currentTimeMillis();

    Conversion conversion = new Conversion(pair, rate.rate, amount, targetAmount, now);

    logger.debug("Saving conversion {}", conversion);

    try {
      conversionRepository.save(conversion);
      ConversionResponse r = new ConversionResponse(conversion);
      logger.info("Converted, {} {} is {} {}", r.sourceAmount, r.source, r.targetAmount, r.target);
      return r;
    } catch (Exception e) {
      throw Errors.Conversion.cannotSaveConversion(
          e,
          Map.of(
              "source", pair.getSource(),
              "target", pair.getTarget(),
              "amount", String.valueOf(amount)));
    }
  }

  @Override
  public ConversionResponse get(long id) {
    logger.info("Getting conversion {}", id);

    Optional<Conversion> maybeConversion;
    try {
      maybeConversion = conversionRepository.findById(id);
    } catch (Exception e) {
      throw Errors.Conversion.cannotReadConversion(e, Map.of("id", String.valueOf(id)));
    }
    Conversion conversion = maybeConversion.orElseThrow(() -> Errors.Conversion.conversionNotFound(id));
    return new ConversionResponse(conversion);
  }

  @Override
  public PagedResponse<ConversionResponse> list(
      LocalDate fromDate, LocalDate toDate, int page, int size, boolean newestFirst) {
    long from = PagingHelper.from(fromDate);
    long to = PagingHelper.to(toDate);

    PageRequest pageRequest =
        PageRequest.of(
            PagingHelper.page(page),
            PagingHelper.size(size, defaultPageSize),
            Sort.by(newestFirst ? Order.desc("createdAt") : Order.asc("createdAt")));

    try {
      Page<Conversion> conversionsPage =
          conversionRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
              from, to, pageRequest);
      return new PagedResponse<>(
          conversionsPage.map(ConversionResponse::new).toList(),
          conversionsPage.getPageable().getPageNumber() + 1,
          conversionsPage.getTotalPages(),
          conversionsPage.getPageable().getPageSize(),
          conversionsPage.getTotalElements());
    } catch (Exception e) {
      throw Errors.Conversion.cannotReadConversion(
          e,
          Map.of(
              "from", String.valueOf(fromDate),
              "to", String.valueOf(toDate),
              "page", String.valueOf(page),
              "size", String.valueOf(size),
              "newestFirst", String.valueOf(newestFirst)));
    }
  }
}
