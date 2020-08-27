package dev.akif.exchange.provider.fixerio;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.akif.exchange.common.Errors;
import dev.akif.exchange.provider.RateProvider;
import dev.akif.exchange.provider.ThirdPartyProvider;
import e.java.E;
import e.java.EOr;

@Component
public class FixerIO extends ThirdPartyProvider implements RateProvider {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final FixerIOConfig config;

    @Autowired
    public FixerIO(FixerIOConfig config) {
        this.config = config;
    }

    @Override
    public EOr<Map<String, Double>> rates() {
        HttpRequest request = request().GET().uri(authenticatedURI("/latest")).build();

        return EOr.catching(
            () -> httpClient.sendAsync(request, BodyHandlers.ofString()).join(),
            t  -> Errors.FixerIO.ratesRequestFailed.cause(E.fromThrowable(t))
        ).flatMap(response ->
            EOr.catching(
                () -> {
                    Map<String, Double> rates = new LinkedHashMap<>();
                    objectMapper.readTree(response.body())
                                .get("rates")
                                .fields()
                                .forEachRemaining(entry -> rates.put(entry.getKey(), entry.getValue().doubleValue()));
                    return rates;
                },
                t -> Errors.FixerIO.parsingRatesFailed.cause(E.fromThrowable(t))
            )
        );
    }

    private URI authenticatedURI(String path) {
        return URI.create(config.host + path + "?access_key=" + config.accessKey);
    }
}
