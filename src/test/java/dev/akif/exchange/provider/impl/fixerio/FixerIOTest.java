package dev.akif.exchange.provider.impl.fixerio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pgssoft.httpclient.HttpClientMock;

import dev.akif.exchange.common.Errors;
import dev.akif.exchange.provider.dto.RateProviderResponse;
import e.java.E;
import e.java.EOr;

public class FixerIOTest {
    private final String baseCurrency = "USD";
    private final String host = "http://mock.fixer.io";
    private final String accessKey = "testkey";
    private final long timeoutInMillis = 1000L;

    private FixerIO fixerIO;
    private HttpClientMock httpClient;

    @BeforeEach
    void setUp() {
        httpClient = new HttpClientMock();
        fixerIO = new FixerIO(httpClient, baseCurrency, host, accessKey, timeoutInMillis);
    }

    @Test
    @DisplayName("getting base currency returns USD")
    void gettingBaseCurrencyReturnsUSD() {
        assertEquals("USD", fixerIO.baseCurrency());
    }

    @Test
    @DisplayName("getting latest rates fails when request fails")
    void gettingLatestRatesFailsWhenRequestFails() {
        httpClient.onGet(host + "/latest?access_key=" + accessKey).doThrowException(new IOException("test"));

        E e = Errors.FixerIO.ratesRequestFailed.cause(E.fromMessage("java.io.IOException: test"));

        assertEquals(e.toEOr(), fixerIO.latestRates());
    }

    @Test
    @DisplayName("getting latest rates fails when parsing fails")
    void gettingLatestRatesFailsWhenParsingFails() {
        httpClient.onGet(host + "/latest?access_key=" + accessKey).doReturn("{\"rates\":\"foo\"}");

        E e = Errors.FixerIO.parsingRatesFailed.cause(E.fromMessage("Cannot construct instance of `java.util.LinkedHashMap` (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value ('foo')\n at [Source: (String)\"{\"rates\":\"foo\"}\"; line: 1, column: 10] (through reference chain: dev.akif.exchange.provider.dto.RateProviderResponse[\"rates\"])"));

        assertEquals(e.toEOr(), fixerIO.latestRates());
    }

    @Test
    @DisplayName("getting latest rates returns rates")
    void gettingLatestRatesReturnsRates() {
        httpClient.onGet(host + "/latest?access_key=" + accessKey).doReturn("{\"rates\":{\"TRY\":7.0,\"EUR\":0.8}}");

        RateProviderResponse expected = new RateProviderResponse(baseCurrency, new LinkedHashMap<>(Map.of("TRY", 7.0, "EUR", 0.8)));

        assertEquals(EOr.from(expected), fixerIO.latestRates());
    }
}
