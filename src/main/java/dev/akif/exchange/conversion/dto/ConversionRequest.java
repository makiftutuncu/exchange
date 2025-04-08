package dev.akif.exchange.conversion.dto;

import java.util.StringJoiner;

public class ConversionRequest {
  public final String source;
  public final String target;
  public final double amount;

  public ConversionRequest(String source, String target, double amount) {
    this.source = source;
    this.target = target;
    this.amount = amount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConversionRequest)) return false;

    ConversionRequest that = (ConversionRequest) o;

    if (!this.source.equals(that.source)) return false;
    if (!this.target.equals(that.target)) return false;

    return this.amount == amount;
  }

  @Override
  public int hashCode() {
    int result = source.hashCode();
    result = 31 * result + target.hashCode();
    result = 31 * result + Double.hashCode(amount);
    return result;
  }

  @Override
  public String toString() {
    return new StringJoiner(",", "{", "}")
        .add("\"source\":\"" + source + "\"")
        .add("\"target\":\"" + target + "\"")
        .add("\"amount\":" + amount)
        .toString();
  }
}
