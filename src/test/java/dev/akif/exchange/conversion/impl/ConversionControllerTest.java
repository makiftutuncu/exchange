package dev.akif.exchange.conversion.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.common.PagedResponse;
import dev.akif.exchange.conversion.ConversionService;
import dev.akif.exchange.conversion.dto.ConversionRequest;
import dev.akif.exchange.conversion.dto.ConversionResponse;
import dev.akif.exchange.conversion.model.Conversion;
import dev.akif.exchange.provider.TimeProvider;
import e.java.E;
import e.java.EOr;

@SpringBootTest
@AutoConfigureMockMvc
public class ConversionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConversionService conversionService;

    @MockBean
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        Mockito.when(timeProvider.now()).thenReturn(123456789L);
    }

    @Test
    @DisplayName("creating a new conversion fails with invalid input")
    void creatingANewConversionFailsWithInvalidInput() throws Exception {
        MockHttpServletResponse response = perform(conversionRequest("foo", "", 0.0));

        E expected = Errors.Common.invalidCurrency.data("source", "foo").time(123456789L);

        assertEquals(expected.code().get(), response.getStatus());
        assertEquals(expected.toString(), response.getContentAsString());
    }

    @Test
    @DisplayName("creating a new conversion fails when service fails")
    void creatingANewConversionFailsWhenServiceFails() throws Exception {
        E e = Errors.Conversion.cannotSaveConversion.time(123456789L);

        Mockito.when(conversionService.convert(new CurrencyPair("USD", "TRY"), 10.0)).thenReturn(e.toEOr());

        MockHttpServletResponse response = perform(conversionRequest("USD", "TRY", 10.0));

        assertEquals(e.code().get(), response.getStatus());
        assertEquals(e.toString(), response.getContentAsString());
    }

    @Test
    @DisplayName("creating a new conversion returns created conversion")
    void creatingANewConversionReturnsCreatedConversion() throws Exception {
        ConversionResponse expected = new ConversionResponse(new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 123456789L));

        Mockito.when(conversionService.convert(new CurrencyPair("USD", "TRY"), 10.0)).thenReturn(EOr.from(expected));

        MockHttpServletResponse response = perform(conversionRequest("USD", "TRY", 10.0));

        assertEquals(201, response.getStatus());
        assertEquals(expected.toString(), response.getContentAsString());
    }

    @Test
    @DisplayName("getting a conversion fails when it is not found")
    void gettingAConversionFailsWhenItIsNotFound() throws Exception {
        E e = Errors.Conversion.conversionNotFound.time(123456789L);

        Mockito.when(conversionService.get(1L)).thenReturn(e.toEOr());

        MockHttpServletResponse response = perform(getRequest(1L));

        assertEquals(e.code().get(), response.getStatus());
        assertEquals(e.toString(), response.getContentAsString());
    }

    @Test
    @DisplayName("getting a conversion returns conversion")
    void gettingAConversionReturnsConversion() throws Exception {
        ConversionResponse expected = new ConversionResponse(new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 123456789L));

        Mockito.when(conversionService.get(2L)).thenReturn(EOr.from(expected));

        MockHttpServletResponse response = perform(getRequest(2L));

        assertEquals(200, response.getStatus());
        assertEquals(expected.toString(), response.getContentAsString());
    }

    @Test
    @DisplayName("listing conversions fails when service fails")
    void listingConversionsFailsWhenServiceFails() throws Exception {
        E e = Errors.Conversion.cannotReadConversion.time(123456789L);

        Mockito.when(conversionService.list(null, null, 1, 5, true)).thenReturn(e.toEOr());

        MockHttpServletResponse response = perform(listRequest(null, null, 1, 5, true));

        assertEquals(e.code().get(), response.getStatus());
        assertEquals(e.toString(), response.getContentAsString());
    }

    @Test
    @DisplayName("listing conversions returns conversions")
    void listingConversionsReturnsConversions() throws Exception {
        Conversion conversion1 = new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 123456789L);
        Conversion conversion2 = new Conversion(new CurrencyPair("EUR", "TRY"), 8.0, 10.0, 80.0, 123456789L);
        conversion1.setId(1L);
        conversion2.setId(2L);

        PagedResponse<ConversionResponse> expected = new PagedResponse<>(List.of(new ConversionResponse(conversion1), new ConversionResponse(conversion2)), 1, 1, 5, 2);

        Mockito.when(conversionService.list(null, null, 1, 5, true)).thenReturn(EOr.from(expected));

        MockHttpServletResponse response = perform(listRequest(null, null, 1, 5, true));

        assertEquals(200, response.getStatus());
        assertEquals(expected.toString(), response.getContentAsString());
    }

    private MockHttpServletRequestBuilder conversionRequest(String source, String target, double amount) {
        return MockMvcRequestBuilders
                .post("/conversions")
                .contentType(MediaType.APPLICATION_JSON)
                .content((new ConversionRequest(source, target, amount)).toString());
    }

    private MockHttpServletRequestBuilder getRequest(long id) {
        return MockMvcRequestBuilders.get("/conversions/" + id);
    }

    private MockHttpServletRequestBuilder listRequest(LocalDate from, LocalDate to, int page, int size, boolean newestFirst) {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/conversions")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .param("newestFirst", String.valueOf(newestFirst));

        if (from != null) request.param("from", from.toString());
        if (to != null)   request.param("to", to.toString());

        return request;
    }

    private MockHttpServletResponse perform(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request).andReturn().getResponse();
    }
}
