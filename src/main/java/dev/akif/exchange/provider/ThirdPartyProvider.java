package dev.akif.exchange.provider;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public abstract class ThirdPartyProvider {
  protected final HttpClient httpClient;
  protected final long timeoutInMillis;

  protected ThirdPartyProvider(HttpClient httpClient, long timeoutInMillis) {
    this.httpClient = httpClient;
    this.timeoutInMillis = timeoutInMillis;
  }

  protected HttpRequest.Builder jsonRequest() {
    return HttpRequest.newBuilder()
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .timeout(Duration.ofMillis(timeoutInMillis));
  }
}
