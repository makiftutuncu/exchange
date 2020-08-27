package dev.akif.exchange.provider.fixerio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FixerIOConfig {
    public final String host;
    public final String accessKey;

    public FixerIOConfig(@Value("${providers.fixerio.host}") String host,
                         @Value("${providers.fixerio.accessKey}") String accessKey) {
        this.host = host;
        this.accessKey = accessKey;
    }
}
