package dev.akif.exchange.provider.impl.fixerio;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akif.exchange.common.Errors;
import dev.akif.exchange.provider.RateProvider;
import dev.akif.exchange.provider.ThirdPartyProvider;
import dev.akif.exchange.provider.dto.RateProviderResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FixerIO extends ThirdPartyProvider implements RateProvider {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final String baseCurrency;
  private final String host;
  private final String accessKey;

  @Autowired
  public FixerIO(
      HttpClient httpClient,
      @Value("${provider.fixerio.baseCurrency}") String baseCurrency,
      @Value("${provider.fixerio.host}") String host,
      @Value("${provider.fixerio.accessKey}") String accessKey,
      @Value("${provider.thirdParty.timeoutInMillis}") long timeoutInMillis) {
    super(httpClient, timeoutInMillis);
    this.baseCurrency = baseCurrency;
    this.host = host;
    this.accessKey = accessKey;
  }

  @Override
  public String baseCurrency() {
    return baseCurrency;
  }

  @Override
  public RateProviderResponse latestRates() {
    logger.info("Getting latest rates");

    URI uri = URI.create(String.format("%s/latest?access_key=%s", host, accessKey));
    HttpRequest request = jsonRequest().GET().uri(uri).build();

    HttpResponse<String> response;
    try {
      response = httpClient.sendAsync(request, BodyHandlers.ofString()).join();
    } catch (Exception e) {
      logger.error("Cannot get latest rates", e);
      throw Errors.FixerIO.ratesRequestFailed(e);
    }

    logger.debug("Got latest rates as {}", response.body());

    try {
      RateProviderResponse rateProviderResponse =
          objectMapper.readValue(response.body(), RateProviderResponse.class);
      rateProviderResponse.setSource(baseCurrency());
      return rateProviderResponse;
    } catch (Exception e) {
      logger.error("Cannot parse latest rates", e);
      throw Errors.FixerIO.parsingRatesFailed(e);
    }
  }
}
