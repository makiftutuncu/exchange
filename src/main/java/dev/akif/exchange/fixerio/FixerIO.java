package dev.akif.exchange.fixerio;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akif.exchange.common.Errors;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class FixerIO {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final HttpClient httpClient;
  private final String baseCurrency;
  private final String host;
  private final String accessKey;
  private final long timeout;

  @Autowired
  public FixerIO(
      HttpClient httpClient,
      @Value("${provider.fixerio.baseCurrency}") String baseCurrency,
      @Value("${provider.fixerio.host}") String host,
      @Value("${provider.fixerio.accessKey}") String accessKey,
      @Value("${provider.thirdParty.timeoutInMillis}") long timeoutInMillis) {
    this.httpClient = httpClient;
    this.baseCurrency = baseCurrency;
    this.host = host;
    this.accessKey = accessKey;
    this.timeout = timeoutInMillis;
  }

  public FixerIOResponse latestRates() {
    logger.info("Getting latest rates");

    URI uri = URI.create(String.format("%s/latest?access_key=%s", host, accessKey));
    HttpRequest request =
        HttpRequest.newBuilder()
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .timeout(Duration.ofMillis(timeout))
            .GET()
            .uri(uri)
            .build();

    HttpResponse<String> response;
    try {
      response = httpClient.sendAsync(request, BodyHandlers.ofString()).join();
    } catch (Exception e) {
      logger.error("Cannot get latest rates", e);
      throw Errors.FixerIO.ratesRequestFailed(e);
    }

    logger.debug("Got latest rates as {}", response.body());

    try {
      FixerIOResponse fixerIOResponse =
          objectMapper.readValue(response.body(), FixerIOResponse.class);
      return new FixerIOResponse(baseCurrency, fixerIOResponse.rates());
    } catch (Exception e) {
      logger.error("Cannot parse latest rates", e);
      throw Errors.FixerIO.parsingRatesFailed(e);
    }
  }
}
