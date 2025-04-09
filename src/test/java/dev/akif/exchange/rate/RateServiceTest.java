package dev.akif.exchange.rate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.fixerio.FixerIO;
import dev.akif.exchange.fixerio.FixerIOResponse;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpStatusCodeException;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:exchangetest;DB_CLOSE_DELAY=-1",
      "rate.freshness-threshold-in-millis=3000"
    })
public class RateServiceTest {
  @MockitoBean private FixerIO fixerIO;

  @Autowired private RateRepository rateRepository;

  @Mock private RateRepository mockRateRepository;

  @Value("${rate.freshness-threshold-in-millis}")
  private long rateFreshnessThresholdInMillis;

  @Autowired private RateService rateService;

  private RateService rateServiceWithMockRepo;

  @BeforeEach
  void setUp() {
    rateServiceWithMockRepo =
        new RateService(fixerIO, mockRateRepository, rateFreshnessThresholdInMillis, "EUR");
    rateRepository.deleteAll();
  }

  @Test
  @DisplayName("getting rate returns 1 when source and target are the same")
  void gettingRateReturns1WhenSourceAndTargetAreTheSame() {
    assertEquals(
        new RateResponse("USD", "USD", 1.0), rateService.rate(new CurrencyPair("USD", "USD")));
  }

  @Test
  @DisplayName("getting rate fails when getting target rate fails")
  void gettingRateFailsWhenGettingTargetRateFails() {
    RateService spy = Mockito.spy(rateService);

    var expected = Errors.Rate.cannotReadRate(null, "EUR", "TRY");

    Mockito.doThrow(expected).when(spy).rateOfBaseTo("TRY");

    var e =
        assertThrows(HttpStatusCodeException.class, () -> spy.rate(new CurrencyPair("USD", "TRY")));
    assertEquals(expected.getStatusCode().value(), e.getStatusCode().value());
    assertEquals(expected.getMessage(), e.getMessage());
  }

  @Test
  @DisplayName("getting rate fails when getting source rate fails")
  void gettingRateFailsWhenGettingSourceRateFails() {
    RateService spy = Mockito.spy(rateService);

    var expected = Errors.Rate.cannotReadRate(null, "EUR", "USD");

    Mockito.doReturn(new RateResponse("EUR", "TRY", 8.0)).when(spy).rateOfBaseTo("TRY");
    Mockito.doThrow(expected).when(spy).rateOfBaseTo("USD");

    var e =
        assertThrows(HttpStatusCodeException.class, () -> spy.rate(new CurrencyPair("USD", "TRY")));
    assertEquals(expected.getStatusCode().value(), e.getStatusCode().value());
    assertEquals(expected.getMessage(), e.getMessage());
  }

  @Test
  @DisplayName("getting rate returns rate")
  void gettingRateReturnsRate() {
    RateService spy = Mockito.spy(rateService);

    Mockito.doReturn(new RateResponse("EUR", "TRY", 8.0)).when(spy).rateOfBaseTo("TRY");
    Mockito.doReturn(new RateResponse("EUR", "USD", 1.2)).when(spy).rateOfBaseTo("USD");

    assertEquals(
        new RateResponse("USD", "TRY", 8.0 / 1.2), spy.rate(new CurrencyPair("USD", "TRY")));
  }

  @Test
  @DisplayName("getting rate of base returns 1 when currency is the same as base")
  void gettingRateOfBaseReturns1WhenCurrencyIsTheSameAsBase() {
    assertEquals(new RateResponse("EUR", "EUR", 1.0), rateService.rateOfBaseTo("EUR"));
  }

  @Test
  @DisplayName("getting rate of base fails when getting from DB fails")
  void gettingRateOfBaseFailsWhenGettingFromDBFails() {
    RateService spy = Mockito.spy(rateService);

    var expected = Errors.Rate.cannotReadRate(null, "EUR", "USD");

    Mockito.doThrow(expected).when(spy).getRateFromDB(new CurrencyPair("EUR", "USD"));

    var e = assertThrows(HttpStatusCodeException.class, () -> spy.rateOfBaseTo("USD"));
    assertEquals(expected.getStatusCode().value(), e.getStatusCode().value());
    assertEquals(expected.getMessage(), e.getMessage());
  }

  @Test
  @DisplayName("getting rate of base returns rate when found on DB")
  void gettingRateOfBaseReturnsRateWhenFoundOnDB() {
    RateService spy = Mockito.spy(rateService);

    Mockito.doReturn(Optional.of(new Rate("EUR", "USD", 1.2, 123456789L)))
        .when(spy)
        .getRateFromDB(new CurrencyPair("EUR", "USD"));

    RateResponse expected = new RateResponse("EUR", "USD", 1.2);

    assertEquals(expected, spy.rateOfBaseTo("USD"));
  }

  @Test
  @DisplayName("getting rate of base fails when getting latest rates fails")
  void gettingRateOfBaseFailsWhenGettingLatestRatesFails() {
    RateService spy = Mockito.spy(rateService);

    var expected = Errors.FixerIO.ratesRequestFailed(null);

    Mockito.doReturn(Optional.empty()).when(spy).getRateFromDB(new CurrencyPair("EUR", "USD"));
    Mockito.doThrow(expected).when(spy).getAndSaveLatestRates();

    var e = assertThrows(HttpStatusCodeException.class, () -> spy.rateOfBaseTo("USD"));
    assertEquals(expected.getStatusCode().value(), e.getStatusCode().value());
    assertEquals(expected.getMessage(), e.getMessage());
  }

  @Test
  @DisplayName("getting rate of base gets latest rates, saves them and returns")
  void gettingRateOfBaseGetsLatestRatesSavesThemAndReturns() {
    RateService spy = Mockito.spy(rateService);

    Mockito.doReturn(Optional.empty()).when(spy).getRateFromDB(new CurrencyPair("EUR", "USD"));
    Mockito.doReturn(new FixerIOResponse("EUR", Map.of("USD", 1.2)))
        .when(spy)
        .getAndSaveLatestRates();

    RateResponse expected = new RateResponse("EUR", "USD", 1.2);

    assertEquals(expected, spy.rateOfBaseTo("USD"));
  }

  @Test
  @DisplayName("getting rate from DB fails when DB fails")
  void gettingRateFromDBFailsWhenDBFails() {
    CurrencyPair id = new CurrencyPair("EUR", "TRY");

    var expected = Errors.Rate.cannotReadRate(new Exception("test"), "EUR", "TRY");

    Mockito.when(mockRateRepository.findById(id)).thenThrow(new RuntimeException("test"));

    var e =
        assertThrows(
            HttpStatusCodeException.class, () -> rateServiceWithMockRepo.getRateFromDB(id));
    assertEquals(expected.getStatusCode().value(), e.getStatusCode().value());
    assertEquals(expected.getMessage(), e.getMessage());
  }

  @Test
  @DisplayName("getting rate from DB returns empty")
  void gettingRateFromDBReturnsEmpty() {
    CurrencyPair id = new CurrencyPair("EUR", "TRY");

    assertEquals(Optional.empty(), rateService.getRateFromDB(id));
  }

  @Test
  @DisplayName("getting rate from DB returns empty when rate is expired")
  void gettingRateFromDBReturnsEmptyWhenRateIsExpired() {
    CurrencyPair id = new CurrencyPair("EUR", "TRY");

    rateRepository.save(new Rate("EUR", "TRY", 8.0, 123L));

    assertEquals(Optional.empty(), rateService.getRateFromDB(id));
  }

  @Test
  @DisplayName("getting rate from DB returns rate")
  void gettingRateFromDBReturnsRate() {
    CurrencyPair id = new CurrencyPair("EUR", "TRY");

    Rate rate =
        rateRepository.save(new Rate("EUR", "TRY", 8.0, System.currentTimeMillis() + 5000L));

    assertEquals(Optional.of(rate), rateService.getRateFromDB(id));
  }

  @Test
  @DisplayName("getting latest rates and saving fails when getting latest rates fails")
  void gettingLatestRatesAndSavingFailsWhenGettingLatestRatesFails() {
    var expected = Errors.FixerIO.ratesRequestFailed(null);

    Mockito.when(fixerIO.latestRates()).thenThrow(expected);

    var e = assertThrows(HttpStatusCodeException.class, () -> rateService.getAndSaveLatestRates());
    assertEquals(expected.getStatusCode().value(), e.getStatusCode().value());
    assertEquals(expected.getMessage(), e.getMessage());
  }

  @Test
  @DisplayName("getting latest rates and saving returns latest rates")
  void gettingLatestRatesAndSavingReturnsLatestRates() {
    FixerIOResponse response = new FixerIOResponse("EUR", Map.of("TRY", 8.0));

    Mockito.when(fixerIO.latestRates()).thenReturn(response);

    assertEquals(response, rateService.getAndSaveLatestRates());

    Optional<Rate> savedRate = rateRepository.findById(new CurrencyPair("EUR", "TRY"));

    assertEquals(
        Optional.of(
            new Rate("EUR", "TRY", 8.0, savedRate.map(r -> r.getUpdatedAt()).orElseThrow())),
        savedRate);
  }
}
