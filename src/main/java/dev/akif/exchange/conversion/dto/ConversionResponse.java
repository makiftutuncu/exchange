package dev.akif.exchange.conversion.dto;

import java.util.StringJoiner;

import dev.akif.exchange.conversion.model.Conversion;

public class ConversionResponse {
    public final long id;
    public final String source;
    public final double sourceAmount;
    public final String target;
    public final double targetAmount;
    public final long createdAt;

    public ConversionResponse(Conversion conversion) {
        this.id           = conversion.getId();
        this.source       = conversion.getSource();
        this.sourceAmount = conversion.getSourceAmount();
        this.target       = conversion.getTarget();
        this.targetAmount = conversion.getTargetAmount();
        this.createdAt    = conversion.getCreatedAt();
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

        return this.createdAt == that.createdAt;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(id);
        result = 31 * result + source.hashCode();
        result = 31 * result + Double.hashCode(sourceAmount);
        result = 31 * result + target.hashCode();
        result = 31 * result + Double.hashCode(targetAmount);
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
            .add("\"createdAt\":" + createdAt)
            .toString();
    }
}
