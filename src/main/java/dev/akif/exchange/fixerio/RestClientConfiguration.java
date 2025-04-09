package dev.akif.exchange.fixerio;

import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
public class RestClientConfiguration {
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public RestClientCustomizer restClientCustomizer(
            @Value("${fixerio.host}") String host,
            @Value("${fixerio.access-key}") String accessKey,
            @Value("${fixerio.timeout-in-millis}") long timeoutInMillis
    ) {
        return builder -> {
            var timeoutSettings =
                    ClientHttpRequestFactorySettings.defaults()
                            .withConnectTimeout(Duration.ofMillis(timeoutInMillis))
                            .withReadTimeout(Duration.ofMillis(timeoutInMillis));

            var requestFactory = ClientHttpRequestFactoryBuilder.detect().build(timeoutSettings);

            ClientHttpRequestInterceptor accessKeyInterceptor =
                    (request, body, execution) -> {
                        HttpRequestWrapper modifiedRequest =
                                new HttpRequestWrapper(request) {
                                    @Override
                                    public URI getURI() {
                                        return UriComponentsBuilder.fromUri(request.getURI())
                                                .queryParam("access_key", accessKey)
                                                .build()
                                                .toUri();
                                    }
                                };
                        return execution.execute(modifiedRequest, body);
                    };

            builder
                    .baseUrl(host)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .requestFactory(requestFactory)
                    .requestInterceptor(accessKeyInterceptor);
        };
    }
}
