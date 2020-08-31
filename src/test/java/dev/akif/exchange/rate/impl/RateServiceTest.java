package dev.akif.exchange.rate.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
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
import org.springframework.boot.test.mock.mockito.MockBean;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.provider.RateProvider;
import dev.akif.exchange.provider.TimeProvider;
import dev.akif.exchange.provider.dto.RateProviderResponse;
import dev.akif.exchange.rate.RateRepository;
import dev.akif.exchange.rate.dto.RateResponse;
import dev.akif.exchange.rate.model.Rate;
import e.java.E;
import e.java.EOr;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:exchangetest;DB_CLOSE_DELAY=-1",
    "rate.freshnessThresholdInMillis=3000"
})
public class RateServiceTest {
    @MockBean
    private RateProvider rateProvider;

    @Autowired
    private RateRepository rateRepository;

    @Mock
    private RateRepository mockRateRepository;

    @MockBean
    private TimeProvider timeProvider;

    @Value("${rate.freshnessThresholdInMillis}")
    private long rateFreshnessThresholdInMillis;

    @Autowired
    private RateServiceImpl rateService;

    private RateServiceImpl rateServiceWithMockRepo;

    @BeforeEach
    void setUp() {
        Mockito.when(rateProvider.baseCurrency()).thenReturn("EUR");
        Mockito.when(timeProvider.now()).thenReturn(123456789L);
        rateServiceWithMockRepo = new RateServiceImpl(rateProvider, mockRateRepository, timeProvider, rateFreshnessThresholdInMillis);
        ((RateCrudRepository) rateRepository).deleteAll();
    }

    @Test
    @DisplayName("getting rate returns 1 when source and target are the same")
    void gettingRateReturns1WhenSourceAndTargetAreTheSame() {
        assertEquals(EOr.from(new RateResponse("USD", "USD", 1.0)), rateService.rate(new CurrencyPair("USD", "USD")));
    }

    @Test
    @DisplayName("getting rate fails when getting target rate fails")
    void gettingRateFailsWhenGettingTargetRateFails() {
        RateServiceImpl spy = Mockito.spy(rateService);

        E e = Errors.Rate.cannotReadRate.data("source", "EUR").data("target", "TRY");

        Mockito.doReturn(e.toEOr()).when(spy).rateOfBaseTo("TRY");

        assertEquals(e.toEOr(), spy.rate(new CurrencyPair("USD", "TRY")));
    }

    @Test
    @DisplayName("getting rate fails when getting source rate fails")
    void gettingRateFailsWhenGettingSourceRateFails() {
        RateServiceImpl spy = Mockito.spy(rateService);

        E e = Errors.Rate.cannotReadRate.data("source", "EUR").data("target", "USD");

        Mockito.doReturn(EOr.from(new RateResponse("EUR", "TRY", 8.0))).when(spy).rateOfBaseTo("TRY");
        Mockito.doReturn(e.toEOr()).when(spy).rateOfBaseTo("USD");

        assertEquals(e.toEOr(), spy.rate(new CurrencyPair("USD", "TRY")));
    }

    @Test
    @DisplayName("getting rate returns rate")
    void gettingRateReturnsRate() {
        RateServiceImpl spy = Mockito.spy(rateService);

        Mockito.doReturn(EOr.from(new RateResponse("EUR", "TRY", 8.0))).when(spy).rateOfBaseTo("TRY");
        Mockito.doReturn(EOr.from(new RateResponse("EUR", "USD", 1.2))).when(spy).rateOfBaseTo("USD");

        assertEquals(EOr.from(new RateResponse("USD", "TRY", 8.0 / 1.2)), spy.rate(new CurrencyPair("USD", "TRY")));
    }

    @Test
    @DisplayName("getting rate of base returns 1 when currency is the same as base")
    void gettingRateOfBaseReturns1WhenCurrencyIsTheSameAsBase() {
        assertEquals(EOr.from(new RateResponse("EUR", "EUR", 1.0)), rateService.rateOfBaseTo("EUR"));
    }

    @Test
    @DisplayName("getting rate of base fails when getting from DB fails")
    void gettingRateOfBaseFailsWhenGettingFromDBFails() {
        RateServiceImpl spy = Mockito.spy(rateService);

        E e = Errors.Rate.cannotReadRate.data("source", "EUR").data("target", "USD");

        Mockito.doReturn(e.toEOr()).when(spy).getRateFromDB(new CurrencyPair("EUR", "USD"));

        assertEquals(e.toEOr(), spy.rateOfBaseTo("USD"));
    }

    @Test
    @DisplayName("getting rate of base returns rate when found on DB")
    void gettingRateOfBaseReturnsRateWhenFoundOnDB() {
        RateServiceImpl spy = Mockito.spy(rateService);

        Mockito.doReturn(EOr.from(Optional.of(new Rate("EUR", "USD", 1.2, 123456789L)))).when(spy).getRateFromDB(new CurrencyPair("EUR", "USD"));

        RateResponse expected = new RateResponse("EUR", "USD", 1.2);

        assertEquals(EOr.from(expected), spy.rateOfBaseTo("USD"));
    }

    @Test
    @DisplayName("getting rate of base fails when getting latest rates fails")
    void gettingRateOfBaseFailsWhenGettingLatestRatesFails() {
        RateServiceImpl spy = Mockito.spy(rateService);

        E e = Errors.FixerIO.ratesRequestFailed;

        Mockito.doReturn(EOr.from(Optional.empty())).when(spy).getRateFromDB(new CurrencyPair("EUR", "USD"));
        Mockito.doReturn(e.toEOr()).when(spy).getAndSaveLatestRates();

        assertEquals(e.toEOr(), spy.rateOfBaseTo("USD"));
    }

    @Test
    @DisplayName("getting rate of base gets latest rates, saves them and returns")
    void gettingRateOfBaseGetsLatestRatesSavesThemAndReturns() {
        RateServiceImpl spy = Mockito.spy(rateService);

        Mockito.doReturn(EOr.from(Optional.empty())).when(spy).getRateFromDB(new CurrencyPair("EUR", "USD"));
        Mockito.doReturn(EOr.from(new RateProviderResponse("EUR", new LinkedHashMap<>(Map.of("USD", 1.2))))).when(spy).getAndSaveLatestRates();

        RateResponse expected = new RateResponse("EUR", "USD", 1.2);

        assertEquals(EOr.from(expected), spy.rateOfBaseTo("USD"));
    }

    @Test
    @DisplayName("getting rate from DB fails when DB fails")
    void gettingRateFromDBFailsWhenDBFails() {
        CurrencyPair id = new CurrencyPair("EUR", "TRY");

        E e = Errors.Rate.cannotReadRate.data("source", "EUR").data("target", "TRY").cause(E.fromMessage("test"));

        Mockito.when(mockRateRepository.findById(id)).thenThrow(new RuntimeException("test"));

        assertEquals(e.toEOr(), rateServiceWithMockRepo.getRateFromDB(id));
    }

    @Test
    @DisplayName("getting rate from DB returns empty")
    void gettingRateFromDBReturnsEmpty() {
        CurrencyPair id = new CurrencyPair("EUR", "TRY");

        assertEquals(EOr.from(Optional.empty()), rateService.getRateFromDB(id));
    }

    @Test
    @DisplayName("getting rate from DB returns empty when rate is expired")
    void gettingRateFromDBReturnsEmptyWhenRateIsExpired() {
        CurrencyPair id = new CurrencyPair("EUR", "TRY");

        rateRepository.save(new Rate("EUR", "TRY", 8.0, 123L));

        assertEquals(EOr.from(Optional.empty()), rateService.getRateFromDB(id));
    }

    @Test
    @DisplayName("getting rate from DB returns rate")
    void gettingRateFromDBReturnsRate() {
        CurrencyPair id = new CurrencyPair("EUR", "TRY");

        Rate rate = rateRepository.save(new Rate("EUR", "TRY", 8.0, 123456789L));

        assertEquals(EOr.from(Optional.of(rate)), rateService.getRateFromDB(id));
    }

    @Test
    @DisplayName("getting latest rates and saving fails when getting latest rates fails")
    void gettingLatestRatesAndSavingFailsWhenGettingLatestRatesFails() {
        E e = Errors.FixerIO.ratesRequestFailed;

        Mockito.when(rateProvider.latestRates()).thenReturn(e.toEOr());

        assertEquals(e.toEOr(), rateService.getAndSaveLatestRates());
    }

    @Test
    @DisplayName("getting latest rates and saving returns latest rates")
    void gettingLatestRatesAndSavingReturnsLatestRates() {
        RateProviderResponse response = new RateProviderResponse("EUR", new LinkedHashMap<>(Map.of("TRY", 8.0)));

        Mockito.when(rateProvider.latestRates()).thenReturn(EOr.from(response));

        assertEquals(EOr.from(response), rateService.getAndSaveLatestRates());

        Optional<Rate> savedRate = rateRepository.findById(new CurrencyPair("EUR", "TRY"));

        assertEquals(Optional.of(new Rate("EUR", "TRY", 8.0, 123456789L)), savedRate);
    }
}
