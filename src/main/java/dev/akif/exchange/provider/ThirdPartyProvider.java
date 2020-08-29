package dev.akif.exchange.provider;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public abstract class ThirdPartyProvider {
    protected final HttpClient httpClient = HttpClient.newHttpClient();

    protected final long timeoutInMillis;

    protected ThirdPartyProvider(long timeoutInMillis) {
        this.timeoutInMillis = timeoutInMillis;
    }

    protected HttpRequest.Builder jsonRequest() {
        return HttpRequest.newBuilder()
                          .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                          .timeout(Duration.ofMillis(timeoutInMillis));
    }
}
