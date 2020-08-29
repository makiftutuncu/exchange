package dev.akif.exchange.rate.dto;

import java.util.Currency;
import java.util.StringJoiner;

import dev.akif.exchange.common.Errors;
import e.java.E;
import e.java.EOr;

public class RateRequest {
    public final String source;
    public final String target;

    private RateRequest(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public static EOr<RateRequest> of(String source, String target) {
        return EOr.catching(
            () -> Currency.getInstance(source),
            t  -> Errors.Common.invalidCurrency.data("source", source).cause(E.fromThrowable(t))
        ).flatMap(s ->
            EOr.catching(
                () -> Currency.getInstance(target),
                t  -> Errors.Common.invalidCurrency.data("target", target)
            )
        ).map(t ->
            new RateRequest(source, target)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RateRequest)) return false;

        RateRequest that = (RateRequest) o;

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
