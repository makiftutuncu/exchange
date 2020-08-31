package dev.akif.exchange.conversion.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.common.PagedResponse;
import dev.akif.exchange.conversion.ConversionRepository;
import dev.akif.exchange.conversion.dto.ConversionResponse;
import dev.akif.exchange.conversion.model.Conversion;
import dev.akif.exchange.provider.TimeProvider;
import dev.akif.exchange.rate.RateService;
import dev.akif.exchange.rate.dto.RateResponse;
import e.java.E;
import e.java.EOr;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:exchangetest;DB_CLOSE_DELAY=-1",
    "conversion.paging.defaultSize=2"
})
public class ConversionServiceTest {
    @Autowired
    private ConversionRepository conversionRepository;

    @Mock
    private ConversionRepository mockConversionRepository;

    @MockBean
    private RateService rateService;

    @MockBean
    private TimeProvider timeProvider;

    @Value("${conversion.paging.defaultSize}")
    protected int defaultPageSize;

    @Autowired
    private ConversionServiceImpl conversionService;

    private ConversionServiceImpl conversionServiceWithMockRepo;

    @BeforeEach
    void setUp() {
        Mockito.when(timeProvider.now()).thenReturn(123456789L);
        conversionServiceWithMockRepo = new ConversionServiceImpl(mockConversionRepository, rateService, timeProvider, defaultPageSize);
        ((ConversionCrudRepository) conversionRepository).deleteAll();
    }

    @Test
    @DisplayName("converting fails when getting rate fails")
    void convertingFailsWhenGettingRateFails() {
        CurrencyPair pair = new CurrencyPair("USD", "TRY");
        E e = Errors.Conversion.conversionNotFound;

        Mockito.when(rateService.rate(pair)).thenReturn(e.toEOr());

        assertEquals(e.toEOr(), conversionService.convert(pair, 10.0));
    }

    @Test
    @DisplayName("converting fails when saving conversion fails")
    void convertingFailsWhenSavingConversionFails() {
        CurrencyPair pair = new CurrencyPair("USD", "TRY");
        Conversion conversion = new Conversion(pair, 7.0, 10.0, 70.0, 123456789L);

        Mockito.when(rateService.rate(pair)).thenReturn(EOr.from(new RateResponse(pair, 7.0)));
        Mockito.when(mockConversionRepository.save(conversion)).thenThrow(new RuntimeException("test"));

        E e = Errors.Conversion.cannotSaveConversion
                .data("source", "USD")
                .data("target", "TRY")
                .data("amount", 10.0)
                .cause(E.fromMessage("test"));

        assertEquals(e.toEOr(), conversionServiceWithMockRepo.convert(pair, 10.0));
    }

    @Test
    @DisplayName("converting returns created conversion")
    void convertingReturnsCreatedConversion() {
        CurrencyPair pair = new CurrencyPair("USD", "TRY");

        Mockito.when(rateService.rate(pair)).thenReturn(EOr.from(new RateResponse(pair, 7.0)));

        Conversion conversion = new Conversion(pair, 7.0, 10.0, 70.0, 123456789L);

        EOr<ConversionResponse> actual   = conversionService.convert(pair, 10.0);
        EOr<ConversionResponse> expected = actual.map(a -> new ConversionResponse(a.id, conversion));

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("getting fails when repository fails")
    void gettingFailsWhenRepositoryFails() {
        E e = Errors.Conversion.cannotReadConversion.data("id", 1).cause(E.fromMessage("test"));

        Mockito.when(mockConversionRepository.findById(1L)).thenThrow(new RuntimeException("test"));

        EOr<ConversionResponse> actual = conversionServiceWithMockRepo.get(1L);

        assertEquals(e.toEOr(), actual);
    }

    @Test
    @DisplayName("getting fails when conversion is not found")
    void gettingFailsWhenConversionIsNotFound() {
        E e = Errors.Conversion.conversionNotFound.data("id", 1);

        EOr<ConversionResponse> actual = conversionService.get(1L);

        assertEquals(e.toEOr(), actual);
    }

    @Test
    @DisplayName("getting returns conversion")
    void gettingReturnsConversion() {
        Conversion conversion = conversionRepository.save(new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 123456789L));

        EOr<ConversionResponse> expected = EOr.from(new ConversionResponse(conversion));
        EOr<ConversionResponse> actual   = conversionService.get(conversion.getId());

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("listing fails when repository fails")
    void listingFailsWhenRepositoryFails() {
        E e = Errors.Conversion.cannotReadConversion
                .data("from", null)
                .data("to", null)
                .data("page", 1)
                .data("size", 2)
                .data("newestFirst", true)
                .cause(E.fromMessage("test"));

        Mockito.when(mockConversionRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(0, Long.MAX_VALUE, PageRequest.of(0, 2, Sort.by(Order.desc("createdAt"))))).thenThrow(new RuntimeException("test"));

        EOr<PagedResponse<ConversionResponse>> actual = conversionServiceWithMockRepo.list(null, null, 1, 2, true);

        assertEquals(e.toEOr(), actual);
    }

    @Test
    @DisplayName("listing returns conversions")
    void listingReturnsConversions() {
        assertEquals(
            EOr.from(new PagedResponse<ConversionResponse>(List.of(), 1, 0, 2, 0)),
            conversionService.list(null, null, 1, 2, true)
        );

        Conversion conversion1 = conversionRepository.save(new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 1598918200000L));
        Conversion conversion2 = conversionRepository.save(new Conversion(new CurrencyPair("TRY", "AED"), 0.5, 20.0, 10.0, 1598918600000L));
        Conversion conversion3 = conversionRepository.save(new Conversion(new CurrencyPair("EUR", "USD"), 1.2, 30.0, 36.0, 1599004800000L));

        EOr<PagedResponse<ConversionResponse>> expected1 = EOr.from(
            new PagedResponse<>(List.of(new ConversionResponse(conversion3), new ConversionResponse(conversion2)), 1, 2, 2, 3)
        );

        EOr<PagedResponse<ConversionResponse>> actual1 = conversionService.list(null, null, 1, 2, true);

        assertEquals(expected1, actual1);

        EOr<PagedResponse<ConversionResponse>> expected2 = EOr.from(
            new PagedResponse<>(List.of(new ConversionResponse(conversion1)), 2, 2, 2, 3)
        );

        EOr<PagedResponse<ConversionResponse>> actual2 = conversionService.list(null, null, 2, 2, true);

        assertEquals(expected2, actual2);

        EOr<PagedResponse<ConversionResponse>> expected3 = EOr.from(
            new PagedResponse<>(List.of(new ConversionResponse(conversion1)), 1, 1, 2, 1)
        );

        EOr<PagedResponse<ConversionResponse>> actual3 = conversionService.list(null, LocalDate.of(2020, 8, 31), 1, 2, false);

        assertEquals(expected3, actual3);

        EOr<PagedResponse<ConversionResponse>> expected4 = EOr.from(
            new PagedResponse<>(List.of(new ConversionResponse(conversion2), new ConversionResponse(conversion3)), 1, 1, 2, 2)
        );

        EOr<PagedResponse<ConversionResponse>> actual4 = conversionService.list(LocalDate.of(2020, 9, 1), null, 1, 2, false);

        assertEquals(expected4, actual4);

        EOr<PagedResponse<ConversionResponse>> expected5 = EOr.from(
            new PagedResponse<>(List.of(), 2, 1, 2, 2)
        );

        EOr<PagedResponse<ConversionResponse>> actual5 = conversionService.list(LocalDate.of(2020, 9, 1), null, 2, 2, false);

        assertEquals(expected5, actual5);
    }
}
