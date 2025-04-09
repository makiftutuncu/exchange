package dev.akif.exchange.provider.impl.fixerio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pgssoft.httpclient.HttpClientMock;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.provider.dto.FixerIOResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpStatusCodeException;

public class FixerIOTest {
  private final String baseCurrency = "USD";
  private final String host = "http://mock.fixer.io";
  private final String accessKey = "testkey";

  private FixerIO fixerIO;
  private HttpClientMock httpClient;

  @BeforeEach
  void setUp() {
    httpClient = new HttpClientMock();
    fixerIO = new FixerIO(httpClient, baseCurrency, host, accessKey, 1000L);
  }

  @Test
  @DisplayName("getting latest rates fails when request fails")
  void gettingLatestRatesFailsWhenRequestFails() {
    httpClient
        .onGet(host + "/latest?access_key=" + accessKey)
        .doThrowException(new IOException("test"));

    var expected = Errors.FixerIO.ratesRequestFailed(new IOException("test"));

    var actual = assertThrows(HttpStatusCodeException.class, () -> fixerIO.latestRates());

    assertEquals(expected.getStatusCode(), actual.getStatusCode());
    assertEquals(expected.getMessage(), actual.getMessage());
  }

  @Test
  @DisplayName("getting latest rates fails when parsing fails")
  void gettingLatestRatesFailsWhenParsingFails() {
    httpClient.onGet(host + "/latest?access_key=" + accessKey).doReturn("{\"rates\":\"foo\"}");

    var expected =
        Errors.FixerIO.parsingRatesFailed(
            new Exception(
                "Cannot construct instance of `java.util.LinkedHashMap` (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value ('foo')\n at [Source: (String)\"{\"rates\":\"foo\"}\"; line: 1, column: 10] (through reference chain: dev.akif.exchange.provider.dto.RateProviderResponse[\"rates\"])"));

    var actual = assertThrows(HttpStatusCodeException.class, () -> fixerIO.latestRates());

    assertEquals(expected.getStatusCode(), actual.getStatusCode());
    assertEquals(expected.getMessage(), actual.getMessage());
  }

  @Test
  @DisplayName("getting latest rates returns rates")
  void gettingLatestRatesReturnsRates() {
    httpClient
        .onGet(host + "/latest?access_key=" + accessKey)
        .doReturn("{\"rates\":{\"TRY\":7.0,\"EUR\":0.8}}");

    FixerIOResponse expected =
        new FixerIOResponse(baseCurrency, new LinkedHashMap<>(Map.of("TRY", 7.0, "EUR", 0.8)));

    assertEquals(expected, fixerIO.latestRates());
  }
}
