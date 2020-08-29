package dev.akif.exchange.rate.model;

import java.io.Serializable;
import java.util.StringJoiner;

public class RateId implements Serializable {
    private String source;
    private String target;

    public RateId() {}

    public RateId(String source, String target) {
        this.source = source;
        this.target = target;
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
        if (!(o instanceof RateId)) return false;

        RateId that = (RateId) o;

        if (!this.source.equals(that.source)) return false;

        return this.target.equals(that.target);
    }

    @Override
    public int hashCode() {
        int result;
        result = source.hashCode();
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
