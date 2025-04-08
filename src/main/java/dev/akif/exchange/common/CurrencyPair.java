package dev.akif.exchange.common;

import java.io.Serializable;
import java.util.Currency;
import java.util.StringJoiner;

public class CurrencyPair implements Serializable {
  private String source;
  private String target;

  public CurrencyPair() {}

  public CurrencyPair(String source, String target) {
    this.source = source;
    this.target = target;
  }

  public static CurrencyPair of(String source, String target) {
    try {
      Currency.getInstance(source);
    } catch (Exception e) {
      throw Errors.Common.invalidCurrency("source", source);
    }
    try {
      Currency.getInstance(target);
    } catch (Exception e) {
      throw Errors.Common.invalidCurrency("target", target);
    }
    return new CurrencyPair(source, target);
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CurrencyPair)) return false;

    CurrencyPair that = (CurrencyPair) o;

    if (!this.source.equals(that.source)) return false;

    return this.target.equals(that.target);
  }

  @Override
  public int hashCode() {
    int result = source.hashCode();
    result = 31 * result + target.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return new StringJoiner(",", "{", "}")
        .add("\"source\":\"" + source + "\"")
        .add("\"target\":\"" + target + "\"")
        .toString();
  }
}
