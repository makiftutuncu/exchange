package dev.akif.exchange.provider;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public abstract class ThirdPartyProvider {
    protected final HttpClient httpClient = HttpClient.newHttpClient();

    protected HttpRequest.Builder request() {
        // TODO: Configure timeout and other default behavior
        return HttpRequest.newBuilder()
                          .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }
}
