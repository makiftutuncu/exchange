package dev.akif.exchange.rate.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.rate.RateService;
import dev.akif.exchange.rate.dto.RateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:exchangetest;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
public class RateControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private RateService rateService;

  @Test
  @DisplayName("getting rates fails with invalid input")
  void gettingRatesFailsWithInvalidInput() throws Exception {
    MockHttpServletResponse response = perform(ratesRequest("foo", ""));

    var expected = Errors.Common.invalidCurrency("source", "foo");

    assertEquals(expected.getStatusCode().value(), response.getStatus());
    assertEquals(expected.getMessage(), response.getContentAsString());
  }

  @Test
  @DisplayName("getting rates fails when service fails")
  void gettingRatesFailsWhenServiceFails() throws Exception {
    CurrencyPair pair = new CurrencyPair("USD", "TRY");

    var e = Errors.Rate.cannotReadRate(null, "USD", "TRY");

    Mockito.when(rateService.rate(pair)).thenThrow(e);

    MockHttpServletResponse response = perform(ratesRequest("USD", "TRY"));

    assertEquals(e.getStatusCode().value(), response.getStatus());
    assertEquals(e.getMessage(), response.getContentAsString());
  }

  @Test
  @DisplayName("getting rates returns rates")
  void gettingRatesReturnsRates() throws Exception {
    CurrencyPair pair = new CurrencyPair("USD", "TRY");

    RateResponse expected = new RateResponse(pair, 7.0);

    Mockito.when(rateService.rate(pair)).thenReturn(expected);

    MockHttpServletResponse response = perform(ratesRequest("USD", "TRY"));

    assertEquals(200, response.getStatus());
    ObjectMapper mapper = new ObjectMapper();
    assertEquals(
        mapper.readTree(expected.toString()), mapper.readTree(response.getContentAsString()));
  }

  private MockHttpServletRequestBuilder ratesRequest(String source, String target) {
    return MockMvcRequestBuilders.get("/rates").param("source", source).param("target", target);
  }

  private MockHttpServletResponse perform(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(request).andReturn().getResponse();
  }
}
