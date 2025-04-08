package dev.akif.exchange.conversion.dto;

import dev.akif.exchange.conversion.model.Conversion;
import java.util.StringJoiner;

public class ConversionResponse {
  public final Long id;
  public final String source;
  public final double sourceAmount;
  public final String target;
  public final double targetAmount;
  public final double rate;
  public final long createdAt;

  public ConversionResponse(Long id, Conversion conversion) {
    this.id = id;
    this.source = conversion.getSource();
    this.sourceAmount = conversion.getSourceAmount();
    this.target = conversion.getTarget();
    this.targetAmount = conversion.getTargetAmount();
    this.rate = conversion.getRate();
    this.createdAt = conversion.getCreatedAt();
  }

  public ConversionResponse(Conversion conversion) {
    this(conversion.getId(), conversion);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConversionResponse)) return false;

    ConversionResponse that = (ConversionResponse) o;

    if (this.id != that.id) return false;
    if (!this.source.equals(that.source)) return false;
    if (this.sourceAmount != that.sourceAmount) return false;
    if (!this.target.equals(that.target)) return false;
    if (this.targetAmount != that.targetAmount) return false;
    if (this.rate != that.rate) return false;

    return this.createdAt == that.createdAt;
  }

  @Override
  public int hashCode() {
    int result = Long.hashCode(id);
    result = 31 * result + source.hashCode();
    result = 31 * result + Double.hashCode(sourceAmount);
    result = 31 * result + target.hashCode();
    result = 31 * result + Double.hashCode(targetAmount);
    result = 31 * result + Double.hashCode(rate);
    result = 31 * result + Long.hashCode(createdAt);
    return result;
  }

  @Override
  public String toString() {
    return new StringJoiner(",", "{", "}")
        .add("\"id\":" + id)
        .add("\"source\":\"" + source + "\"")
        .add("\"sourceAmount\":" + sourceAmount)
        .add("\"target\":\"" + target + "\"")
        .add("\"targetAmount\":" + targetAmount)
        .add("\"rate\":" + rate)
        .add("\"createdAt\":" + createdAt)
        .toString();
  }
}
