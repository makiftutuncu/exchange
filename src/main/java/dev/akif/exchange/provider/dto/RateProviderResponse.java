package dev.akif.exchange.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.LinkedHashMap;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RateProviderResponse {
  private String source;
  private LinkedHashMap<String, Double> rates;

  public RateProviderResponse() {}

  public RateProviderResponse(String source, LinkedHashMap<String, Double> rates) {
    this.source = source;
    this.rates = rates;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public LinkedHashMap<String, Double> getRates() {
    return rates;
  }

  public void setRates(LinkedHashMap<String, Double> rates) {
    this.rates = rates;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RateProviderResponse)) return false;

    RateProviderResponse that = (RateProviderResponse) o;

    if (!this.source.equals(that.source)) return false;

    return this.rates.equals(that.rates);
  }

  @Override
  public int hashCode() {
    int result = source.hashCode();
    result = 31 * result + rates.hashCode();
    return result;
  }

  @Override
  public String toString() {
    StringJoiner ratesJoiner = new StringJoiner(",", "{", "}");
    rates.forEach((key, value) -> ratesJoiner.add("\"" + key + "\":" + value));

    return new StringJoiner(",", "{", "}")
        .add("\"source\":\"" + source + "\"")
        .add("\"rates\":" + ratesJoiner.toString())
        .toString();
  }
}
