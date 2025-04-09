package dev.akif.exchange.fixerio;

import dev.akif.exchange.common.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FixerIO {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final RestClient restClient;
  private final String baseCurrency;

  @Autowired
  public FixerIO(RestClient restClient, @Value("${rate.base-currency}") String baseCurrency) {
    this.restClient = restClient;
    this.baseCurrency = baseCurrency;
  }

  public FixerIOResponse latestRates() {
    logger.info("Getting latest rates");
    try {
      var response =
          restClient
              .get()
              .uri("/latest")
              .retrieve()
              .body(FixerIOResponse.class);
      logger.debug("Got latest rates as {}", response);
      return new FixerIOResponse(baseCurrency, response.rates());
    } catch (Exception e) {
      logger.error("Cannot get latest rates", e);
      throw Errors.FixerIO.ratesRequestFailed(e);
    }
  }
}
