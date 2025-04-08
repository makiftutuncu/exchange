package dev.akif.exchange.rate.model;

import dev.akif.exchange.common.CurrencyPair;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.StringJoiner;

@Entity
@Table(name = "rates")
@IdClass(CurrencyPair.class)
public class Rate {
  @Id
  @Column(name = "source")
  private String source;

  @Id
  @Column(name = "target")
  private String target;

  @Column(name = "rate")
  private double rate;

  @Column(name = "updated_at")
  private long updatedAt;

  public Rate() {}

  public Rate(String source, String target, double rate, long updatedAt) {
    this.source = source;
    this.target = target;
    this.rate = rate;
    this.updatedAt = updatedAt;
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

  public double getRate() {
    return rate;
  }

  public void setRate(double rate) {
    this.rate = rate;
  }

  public long getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(long updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Rate)) return false;

    Rate that = (Rate) o;

    if (!this.source.equals(that.source)) return false;
    if (!this.target.equals(that.target)) return false;
    if (this.rate != that.rate) return false;

    return this.updatedAt == that.updatedAt;
  }

  @Override
  public int hashCode() {
    int result = source.hashCode();
    result = 31 * result + target.hashCode();
    result = 31 * result + Double.hashCode(rate);
    result = 31 * result + Long.hashCode(updatedAt);
    return result;
  }

  @Override
  public String toString() {
    return new StringJoiner(",", "{", "}")
        .add("\"source\":\"" + source + "\"")
        .add("\"target\":\"" + target + "\"")
        .add("\"rate\":" + rate)
        .add("\"updatedAt\":" + updatedAt)
        .toString();
  }
}
