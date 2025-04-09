package dev.akif.exchange.conversion;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.common.PagedResponse;
import dev.akif.exchange.rate.RateResponse;
import dev.akif.exchange.rate.RateService;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
public class ConversionService {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ConversionRepository conversionRepository;
  private final RateService rateService;
  private final int defaultPageSize;

  @Autowired
  public ConversionService(
      ConversionRepository conversionRepository,
      RateService rateService,
      @Value("${conversion.paging.default-size}") int defaultPageSize) {
    this.conversionRepository = conversionRepository;
    this.rateService = rateService;
    this.defaultPageSize = defaultPageSize;
  }

  public ConversionResponse convert(CurrencyPair pair, double amount) {
    logger.info("Converting {} {} to {}", amount, pair.source(), pair.target());

    RateResponse rate = rateService.rate(pair);
    double targetAmount = amount * rate.rate();
    long now = System.currentTimeMillis();

    Conversion conversion = new Conversion(pair, rate.rate(), amount, targetAmount, now);

    logger.debug("Saving conversion {}", conversion);

    try {
      conversionRepository.save(conversion);
      ConversionResponse r = new ConversionResponse(conversion);
      logger.info(
          "Converted, {} {} is {} {}", r.sourceAmount(), r.source(), r.targetAmount(), r.target());
      return r;
    } catch (Exception e) {
      throw Errors.Conversion.cannotSaveConversion(
          e,
          Map.of(
              "source", pair.source(),
              "target", pair.target(),
              "amount", String.valueOf(amount)));
    }
  }

  public ConversionResponse get(long id) {
    logger.info("Getting conversion {}", id);

    Optional<Conversion> maybeConversion;
    try {
      maybeConversion = conversionRepository.findById(id);
    } catch (Exception e) {
      throw Errors.Conversion.cannotReadConversion(e, Map.of("id", String.valueOf(id)));
    }
    Conversion conversion =
        maybeConversion.orElseThrow(() -> Errors.Conversion.conversionNotFound(id));
    return new ConversionResponse(conversion);
  }

  public PagedResponse<ConversionResponse> list(
      LocalDate fromDate, LocalDate toDate, int page, int size, boolean newestFirst) {
    long from =
        fromDate == null ? 0L : fromDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond() * 1000;
    long to =
        toDate == null
            ? Long.MAX_VALUE
            : toDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond() * 1000;

    PageRequest pageRequest =
        PageRequest.of(
            page <= 0 ? 0 : page - 1,
            size <= 0 || size > 50 ? defaultPageSize : size,
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
