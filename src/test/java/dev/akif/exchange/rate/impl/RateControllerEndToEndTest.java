package dev.akif.exchange.rate.impl;

import org.hamcrest.comparator.ComparatorMatcherBuilder;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:exchangetest;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
public class RateControllerEndToEndTest {
  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("getting rates returns rates")
  void gettingRatesReturnsRates() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/rates").param("source", "USD").param("target", "TRY"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.source", Is.is("USD")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.target", Is.is("TRY")))
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$.rate", ComparatorMatcherBuilder.comparedBy(Double::compare).greaterThan(0.0)));
  }
}
