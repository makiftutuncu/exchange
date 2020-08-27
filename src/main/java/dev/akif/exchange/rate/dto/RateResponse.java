package dev.akif.exchange.rate.dto;

import java.util.StringJoiner;

public class RateResponse {
    public final String source;
    public final String target;
    public final double rate;

    public RateResponse(String source, String target, double rate) {
        this.source = source;
        this.target = target;
        this.rate   = rate;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RateResponse)) return false;

        RateResponse that = (RateResponse) o;

        if (!this.source.equals(that.source)) return false;
        if (!this.target.equals(that.target)) return false;

        return this.rate == that.rate;
    }

    @Override public int hashCode() {
        int result;
        result = source.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + Double.hashCode(rate);
        return result;
    }

    @Override public String toString() {
        return new StringJoiner(",", "{", "}")
            .add("\"source\":\"" + source + "\"")
            .add("\"target\":\"" + target + "\"")
            .add("\"rate\":" + target + "")
            .toString();
    }
}
