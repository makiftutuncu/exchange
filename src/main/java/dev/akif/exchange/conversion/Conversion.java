package dev.akif.exchange.conversion;

import dev.akif.exchange.common.CurrencyPair;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.StringJoiner;

@Entity
@Table(name = "conversions")
public class Conversion {
  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "source")
  private String source;

  @Column(name = "target")
  private String target;

  @Column(name = "rate")
  private double rate;

  @Column(name = "source_amount")
  private double sourceAmount;

  @Column(name = "target_amount")
  private double targetAmount;

  @Column(name = "created_at")
  private long createdAt;

  public Conversion() {}

  public Conversion(
      CurrencyPair pair, double rate, double sourceAmount, double targetAmount, long createdAt) {
    this.source = pair.source();
    this.target = pair.target();
    this.rate = rate;
    this.sourceAmount = sourceAmount;
    this.targetAmount = targetAmount;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public double getSourceAmount() {
    return sourceAmount;
  }

  public void setSourceAmount(double sourceAmount) {
    this.sourceAmount = sourceAmount;
  }

  public double getTargetAmount() {
    return targetAmount;
  }

  public void setTargetAmount(double targetAmount) {
    this.targetAmount = targetAmount;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Conversion)) return false;

    Conversion that = (Conversion) o;

    if (this.id != that.id) return false;
    if (!this.source.equals(that.source)) return false;
    if (!this.target.equals(that.target)) return false;
    if (this.rate != that.rate) return false;
    if (this.sourceAmount != that.sourceAmount) return false;
    if (this.targetAmount != that.targetAmount) return false;

    return this.createdAt == that.createdAt;
  }

  @Override
  public int hashCode() {
    int result = Long.hashCode(id);
    result = 31 * result + source.hashCode();
    result = 31 * result + target.hashCode();
    result = 31 * result + Double.hashCode(rate);
    result = 31 * result + Double.hashCode(sourceAmount);
    result = 31 * result + Double.hashCode(targetAmount);
    result = 31 * result + Long.hashCode(createdAt);
    return result;
  }

  @Override
  public String toString() {
    return new StringJoiner(",", "{", "}")
        .add("\"id\":" + id)
        .add("\"source\":\"" + source + "\"")
        .add("\"target\":\"" + target + "\"")
        .add("\"rate\":" + rate)
        .add("\"sourceAmount\":" + sourceAmount)
        .add("\"targetAmount\":" + targetAmount)
        .add("\"createdAt\":" + createdAt)
        .toString();
  }
}
