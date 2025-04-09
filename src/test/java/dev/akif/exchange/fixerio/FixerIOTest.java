package dev.akif.exchange.fixerio;

import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import dev.akif.exchange.common.Errors;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpStatusCodeException;

@RestClientTest(
    components = {FixerIO.class, RestClientConfiguration.class},
    properties = {"rate.base-currency=USD", "fixerio.access-key=test"})
public class FixerIOTest {
  @Autowired private FixerIO fixerIO;
  @Autowired private MockRestServiceServer server;

  @BeforeEach
  void setUp() {
    server.reset();
  }

  @Test
  @DisplayName("getting latest rates fails when request fails")
  void gettingLatestRatesFailsWhenRequestFails() {
    server.expect(requestTo(endsWith("/latest?access_key=test"))).andRespond(withBadRequest());

    var expected = Errors.FixerIO.ratesRequestFailed(new IOException("test"));

    var actual = assertThrows(HttpStatusCodeException.class, () -> fixerIO.latestRates());

    server.verify();
    assertEquals(expected.getStatusCode(), actual.getStatusCode());
    assertEquals(expected.getMessage(), actual.getMessage());
  }

  @Test
  @DisplayName("getting latest rates returns rates")
  void gettingLatestRatesReturnsRates() {
    server
        .expect(requestTo(endsWith("/latest?access_key=test")))
        .andRespond(
            withSuccess("{\"rates\":{\"TRY\":7.0,\"EUR\":0.8}}", MediaType.APPLICATION_JSON));

    var expected = new FixerIOResponse("USD", Map.of("TRY", 7.0, "EUR", 0.8));
    var actual = fixerIO.latestRates();

    server.verify();
    assertEquals(expected, actual);
  }
}
