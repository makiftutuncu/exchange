package dev.akif.exchange.conversion.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.common.PagedResponse;
import dev.akif.exchange.conversion.dto.ConversionResponse;
import dev.akif.exchange.conversion.model.Conversion;
import dev.akif.exchange.rate.dto.RateResponse;
import dev.akif.exchange.rate.impl.RateService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpStatusCodeException;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:exchangetest;DB_CLOSE_DELAY=-1",
      "conversion.paging.defaultSize=2"
    })
public class ConversionServiceTest {
  @Autowired private ConversionRepository conversionRepository;

  @Mock private ConversionRepository mockConversionRepository;

  @MockitoBean private RateService rateService;

  @Value("${conversion.paging.defaultSize}")
  private int defaultPageSize;

  @Autowired private ConversionService conversionService;

  private ConversionService conversionServiceWithMockRepo;

  @BeforeEach
  void setUp() {
    conversionServiceWithMockRepo =
        new ConversionService(mockConversionRepository, rateService, defaultPageSize);
    conversionRepository.deleteAll();
  }

  @Test
  @DisplayName("converting fails when getting rate fails")
  void convertingFailsWhenGettingRateFails() {
    CurrencyPair pair = new CurrencyPair("USD", "TRY");
    var expected = Errors.Conversion.conversionNotFound(1L);

    Mockito.when(rateService.rate(pair)).thenThrow(expected);

    var e =
        assertThrows(HttpStatusCodeException.class, () -> conversionService.convert(pair, 10.0));
    assertEquals(expected.getStatusCode().value(), e.getStatusCode().value());
    assertEquals(expected.getMessage(), e.getMessage());
  }

  @Test
  @DisplayName("converting fails when saving conversion fails")
  void convertingFailsWhenSavingConversionFails() {
    CurrencyPair pair = new CurrencyPair("USD", "TRY");

    Mockito.when(rateService.rate(pair)).thenReturn(new RateResponse(pair, 7.0));
    Mockito.when(mockConversionRepository.save(any())).thenThrow(new RuntimeException("test"));

    var expected =
        Errors.Conversion.cannotSaveConversion(
            null, Map.of("source", "USD", "target", "TRY", "amount", "10.0"));

    var e =
        assertThrows(
            HttpStatusCodeException.class, () -> conversionServiceWithMockRepo.convert(pair, 10.0));
    assertEquals(expected.getStatusCode().value(), e.getStatusCode().value());
    assertEquals(expected.getMessage(), e.getMessage());
  }

  @Test
  @DisplayName("converting returns created conversion")
  void convertingReturnsCreatedConversion() {
    CurrencyPair pair = new CurrencyPair("USD", "TRY");

    Mockito.when(rateService.rate(pair)).thenReturn(new RateResponse(pair, 7.0));

    ConversionResponse actual = conversionService.convert(pair, 10.0);

    Conversion conversion = new Conversion(pair, 7.0, 10.0, 70.0, actual.createdAt());
    ConversionResponse expected = new ConversionResponse(actual.id(), conversion);

    assertEquals(expected, actual);
  }

  @Test
  @DisplayName("getting fails when repository fails")
  void gettingFailsWhenRepositoryFails() {
    var expected = Errors.Conversion.cannotReadConversion(new Exception("test"), Map.of("id", "1"));

    Mockito.when(mockConversionRepository.findById(1L)).thenThrow(new RuntimeException("test"));

    var e =
        assertThrows(HttpStatusCodeException.class, () -> conversionServiceWithMockRepo.get(1L));
    assertEquals(expected.getStatusCode().value(), e.getStatusCode().value());
    assertEquals(expected.getMessage(), e.getMessage());
  }

  @Test
  @DisplayName("getting fails when conversion is not found")
  void gettingFailsWhenConversionIsNotFound() {
    var expected = Errors.Conversion.conversionNotFound(1);

    var e = assertThrows(HttpStatusCodeException.class, () -> conversionService.get(1L));
    assertEquals(expected.getStatusCode().value(), e.getStatusCode().value());
    assertEquals(expected.getMessage(), e.getMessage());
  }

  @Test
  @DisplayName("getting returns conversion")
  void gettingReturnsConversion() {
    Conversion conversion =
        conversionRepository.save(
            new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 123456789L));

    ConversionResponse expected = new ConversionResponse(conversion);
    ConversionResponse actual = conversionService.get(conversion.getId());

    assertEquals(expected, actual);
  }

  @Test
  @DisplayName("listing fails when repository fails")
  void listingFailsWhenRepositoryFails() {
    var expected =
        Errors.Conversion.cannotReadConversion(
            new Exception("test"),
            Map.of("from", "null", "to", "null", "page", "1", "size", "2", "newestFirst", "true"));

    Mockito.when(
            mockConversionRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                0, Long.MAX_VALUE, PageRequest.of(0, 2, Sort.by(Order.desc("createdAt")))))
        .thenThrow(new RuntimeException("test"));

    var e =
        assertThrows(
            HttpStatusCodeException.class,
            () -> conversionServiceWithMockRepo.list(null, null, 1, 2, true));
    assertEquals(expected.getStatusCode().value(), e.getStatusCode().value());
    assertEquals(expected.getMessage(), e.getMessage());
  }

  @Test
  @DisplayName("listing returns conversions")
  void listingReturnsConversions() {
    assertEquals(
        new PagedResponse<ConversionResponse>(List.of(), 1, 0, 2, 0),
        conversionService.list(null, null, 1, 2, true));

    Conversion conversion1 =
        conversionRepository.save(
            new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 1598918200000L));
    Conversion conversion2 =
        conversionRepository.save(
            new Conversion(new CurrencyPair("TRY", "AED"), 0.5, 20.0, 10.0, 1598918600000L));
    Conversion conversion3 =
        conversionRepository.save(
            new Conversion(new CurrencyPair("EUR", "USD"), 1.2, 30.0, 36.0, 1599004800000L));

    PagedResponse<ConversionResponse> expected1 =
        new PagedResponse<>(
            List.of(new ConversionResponse(conversion3), new ConversionResponse(conversion2)),
            1,
            2,
            2,
            3);

    PagedResponse<ConversionResponse> actual1 = conversionService.list(null, null, 1, 2, true);

    assertEquals(expected1, actual1);

    PagedResponse<ConversionResponse> expected2 =
        new PagedResponse<>(List.of(new ConversionResponse(conversion1)), 2, 2, 2, 3);

    PagedResponse<ConversionResponse> actual2 = conversionService.list(null, null, 2, 2, true);

    assertEquals(expected2, actual2);

    PagedResponse<ConversionResponse> expected3 =
        new PagedResponse<>(List.of(new ConversionResponse(conversion1)), 1, 1, 2, 1);

    PagedResponse<ConversionResponse> actual3 =
        conversionService.list(null, LocalDate.of(2020, 8, 31), 1, 2, false);

    assertEquals(expected3, actual3);

    PagedResponse<ConversionResponse> expected4 =
        new PagedResponse<>(
            List.of(new ConversionResponse(conversion2), new ConversionResponse(conversion3)),
            1,
            1,
            2,
            2);

    PagedResponse<ConversionResponse> actual4 =
        conversionService.list(LocalDate.of(2020, 9, 1), null, 1, 2, false);

    assertEquals(expected4, actual4);

    PagedResponse<ConversionResponse> expected5 = new PagedResponse<>(List.of(), 2, 1, 2, 2);

    PagedResponse<ConversionResponse> actual5 =
        conversionService.list(LocalDate.of(2020, 9, 1), null, 2, 2, false);

    assertEquals(expected5, actual5);
  }
}
