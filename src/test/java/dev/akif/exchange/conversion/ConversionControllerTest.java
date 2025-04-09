package dev.akif.exchange.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.common.PagedResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:exchangetest;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
public class ConversionControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private ConversionService conversionService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("creating a new conversion fails with invalid input")
  void creatingANewConversionFailsWithInvalidInput() throws Exception {
    MockHttpServletResponse response = perform(conversionRequest("foo", "", 0.0));

    var expected = Errors.Common.invalidCurrency("source", "foo");

    assertEquals(expected.getStatusCode().value(), response.getStatus());
    assertEquals(expected.getMessage(), response.getContentAsString());
  }

  @Test
  @DisplayName("creating a new conversion fails when service fails")
  void creatingANewConversionFailsWhenServiceFails() throws Exception {
    var e = Errors.Conversion.cannotSaveConversion(null, Map.of());

    Mockito.when(conversionService.convert(new CurrencyPair("USD", "TRY"), 10.0)).thenThrow(e);

    MockHttpServletResponse response = perform(conversionRequest("USD", "TRY", 10.0));

    assertEquals(e.getStatusCode().value(), response.getStatus());
    assertEquals(e.getMessage(), response.getContentAsString());
  }

  @Test
  @DisplayName("creating a new conversion returns created conversion")
  void creatingANewConversionReturnsCreatedConversion() throws Exception {
    ConversionResponse expected =
        new ConversionResponse(
            new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 123456789L));

    Mockito.when(conversionService.convert(new CurrencyPair("USD", "TRY"), 10.0))
        .thenReturn(expected);

    MockHttpServletResponse response = perform(conversionRequest("USD", "TRY", 10.0));

    assertEquals(201, response.getStatus());
    assertEquals(objectMapper.writeValueAsString(expected), response.getContentAsString());
  }

  @Test
  @DisplayName("getting a conversion fails when it is not found")
  void gettingAConversionFailsWhenItIsNotFound() throws Exception {
    var e = Errors.Conversion.conversionNotFound(1L);

    Mockito.when(conversionService.get(1L)).thenThrow(e);

    MockHttpServletResponse response = perform(getRequest(1L));

    assertEquals(e.getStatusCode().value(), response.getStatus());
    assertEquals(e.getMessage(), response.getContentAsString());
  }

  @Test
  @DisplayName("getting a conversion returns conversion")
  void gettingAConversionReturnsConversion() throws Exception {
    ConversionResponse expected =
        new ConversionResponse(
            new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 123456789L));

    Mockito.when(conversionService.get(2L)).thenReturn(expected);

    MockHttpServletResponse response = perform(getRequest(2L));

    assertEquals(200, response.getStatus());
    assertEquals(objectMapper.writeValueAsString(expected), response.getContentAsString());
  }

  @Test
  @DisplayName("listing conversions fails when service fails")
  void listingConversionsFailsWhenServiceFails() throws Exception {
    var e = Errors.Conversion.cannotReadConversion(null, Map.of());

    Mockito.when(conversionService.list(null, null, 1, 5, true)).thenThrow(e);

    MockHttpServletResponse response = perform(listRequest(null, null, 1, 5, true));

    assertEquals(e.getStatusCode().value(), response.getStatus());
    assertEquals(e.getMessage(), response.getContentAsString());
  }

  @Test
  @DisplayName("listing conversions returns conversions")
  void listingConversionsReturnsConversions() throws Exception {
    Conversion conversion1 =
        new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 123456789L);
    Conversion conversion2 =
        new Conversion(new CurrencyPair("EUR", "TRY"), 8.0, 10.0, 80.0, 123456789L);
    conversion1.setId(1L);
    conversion2.setId(2L);

    PagedResponse<ConversionResponse> expected =
        new PagedResponse<>(
            List.of(new ConversionResponse(conversion1), new ConversionResponse(conversion2)),
            1,
            1,
            5,
            2);

    Mockito.when(conversionService.list(null, null, 1, 5, true)).thenReturn(expected);

    MockHttpServletResponse response = perform(listRequest(null, null, 1, 5, true));

    assertEquals(200, response.getStatus());
    assertEquals(objectMapper.writeValueAsString(expected), response.getContentAsString());
  }

  private MockHttpServletRequestBuilder conversionRequest(
      String source, String target, double amount) throws JsonProcessingException {
    return MockMvcRequestBuilders.post("/conversions")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new ConversionRequest(source, target, amount)));
  }

  private MockHttpServletRequestBuilder getRequest(long id) {
    return MockMvcRequestBuilders.get("/conversions/" + id);
  }

  private MockHttpServletRequestBuilder listRequest(
      LocalDate from, LocalDate to, int page, int size, boolean newestFirst) {
    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.get("/conversions")
            .param("page", String.valueOf(page))
            .param("size", String.valueOf(size))
            .param("newestFirst", String.valueOf(newestFirst));

    if (from != null) request.param("from", from.toString());
    if (to != null) request.param("to", to.toString());

    return request;
  }

  private MockHttpServletResponse perform(MockHttpServletRequestBuilder request) throws Exception {
    return mockMvc.perform(request).andReturn().getResponse();
  }
}
