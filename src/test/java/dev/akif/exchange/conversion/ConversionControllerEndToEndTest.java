package dev.akif.exchange.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.common.PagedResponse;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
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
public class ConversionControllerEndToEndTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private ConversionRepository conversionRepository;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    conversionRepository.deleteAll();
  }

  @Test
  @DisplayName("creating a new conversion returns created conversion")
  void creatingANewConversionReturnsCreatedConversion() throws Exception {
    ConversionRequest request = new ConversionRequest("USD", "TRY", 10.0);

    String response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/conversions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

    ConversionResponse conversionResponse =
        objectMapper.readValue(response, ConversionResponse.class);

    assertTrue(conversionResponse.id() > 0L);
    assertEquals("USD", conversionResponse.source());
    assertEquals("TRY", conversionResponse.target());
    assertEquals(
        conversionResponse.rate() * conversionResponse.sourceAmount(),
        conversionResponse.targetAmount());
    assertTrue(conversionResponse.createdAt() > 0L);
  }

  @Test
  @DisplayName("getting a conversion fails when it is not found")
  void gettingAConversionFailsWhenItIsNotFound() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/conversions/1"))
        .andExpect(MockMvcResultMatchers.status().isNotFound())
        .andExpect(MockMvcResultMatchers.content().string("Cannot find conversion 1"));
  }

  @Test
  @DisplayName("getting a conversion returns conversion")
  void gettingAConversionReturnsConversion() throws Exception {
    Conversion conversion =
        conversionRepository.save(
            new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 123456789L));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/conversions/" + conversion.getId()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.id", Is.is(conversion.getId().intValue())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.source", Is.is(conversion.getSource())))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.sourceAmount", Is.is(conversion.getSourceAmount())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.target", Is.is(conversion.getTarget())))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.targetAmount", Is.is(conversion.getTargetAmount())))
        .andExpect(MockMvcResultMatchers.jsonPath("$.rate", Is.is(conversion.getRate())))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.createdAt", Is.is((int) conversion.getCreatedAt())));
  }

  @Test
  @DisplayName("listing conversions returns conversions")
  void listingConversionsReturnsConversions() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/conversions")
                .param("page", String.valueOf(1))
                .param("size", String.valueOf(2))
                .param("newestFirst", String.valueOf(true)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            MockMvcResultMatchers.content()
                .json(
                    objectMapper.writeValueAsString(
                        new PagedResponse<ConversionResponse>(List.of(), 1, 0, 2, 0))));

    Conversion conversion1 =
        conversionRepository.save(
            new Conversion(new CurrencyPair("USD", "TRY"), 7.0, 10.0, 70.0, 1598918200000L));
    Conversion conversion2 =
        conversionRepository.save(
            new Conversion(new CurrencyPair("TRY", "AED"), 0.5, 20.0, 10.0, 1598918600000L));
    Conversion conversion3 =
        conversionRepository.save(
            new Conversion(new CurrencyPair("EUR", "USD"), 1.2, 30.0, 36.0, 1599004800000L));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/conversions")
                .param("page", String.valueOf(1))
                .param("size", String.valueOf(2))
                .param("newestFirst", String.valueOf(true)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            MockMvcResultMatchers.content()
                .json(
                    objectMapper.writeValueAsString(
                        new PagedResponse<>(
                            List.of(
                                new ConversionResponse(conversion3),
                                new ConversionResponse(conversion2)),
                            1,
                            2,
                            2,
                            3))));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/conversions")
                .param("page", String.valueOf(2))
                .param("size", String.valueOf(2))
                .param("newestFirst", String.valueOf(true)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            MockMvcResultMatchers.content()
                .json(
                    objectMapper.writeValueAsString(
                        new PagedResponse<>(
                            List.of(new ConversionResponse(conversion1)), 2, 2, 2, 3))));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/conversions")
                .param("page", String.valueOf(1))
                .param("size", String.valueOf(2))
                .param("to", LocalDate.of(2020, 8, 31).toString())
                .param("newestFirst", String.valueOf(false)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            MockMvcResultMatchers.content()
                .json(
                    objectMapper.writeValueAsString(
                        new PagedResponse<>(
                            List.of(new ConversionResponse(conversion1)), 1, 1, 2, 1))));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/conversions")
                .param("page", String.valueOf(1))
                .param("size", String.valueOf(2))
                .param("from", LocalDate.of(2020, 9, 1).toString())
                .param("newestFirst", String.valueOf(false)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            MockMvcResultMatchers.content()
                .json(
                    objectMapper.writeValueAsString(
                        new PagedResponse<>(
                            List.of(
                                new ConversionResponse(conversion2),
                                new ConversionResponse(conversion3)),
                            1,
                            1,
                            2,
                            2))));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/conversions")
                .param("page", String.valueOf(2))
                .param("size", String.valueOf(2))
                .param("from", LocalDate.of(2020, 9, 1).toString())
                .param("newestFirst", String.valueOf(false)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            MockMvcResultMatchers.content()
                .json(
                    objectMapper.writeValueAsString(
                        new PagedResponse<ConversionResponse>(List.of(), 2, 1, 2, 2))));
  }
}
