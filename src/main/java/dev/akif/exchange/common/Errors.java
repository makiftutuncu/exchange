package dev.akif.exchange.common;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

public interface Errors {
  interface Common {
    static HttpStatusCodeException invalidCurrency(String type, String value) {
      return new HttpClientErrorException(
          type + " currency " + value + " is invalid",
          HttpStatus.BAD_REQUEST,
          "bad-request",
          null,
          null,
          null);
    }
  }

  interface Conversion {
    static HttpStatusCodeException conversionNotFound(long id) {
      return new HttpClientErrorException(
          "Cannot find conversion " + id, HttpStatus.NOT_FOUND, "not-found", null, null, null);
    }

    static HttpStatusCodeException cannotReadConversion(
        Exception cause, Map<String, String> details) {
      HttpStatusCodeException exception =
          new HttpClientErrorException(
              "Cannot read conversion " + details,
              HttpStatus.NOT_FOUND,
              "not-found",
              null,
              null,
              null);
      exception.initCause(cause);
      return exception;
    }

    static HttpStatusCodeException cannotSaveConversion(
        Exception cause, Map<String, String> details) {
      HttpStatusCodeException exception =
          new HttpServerErrorException(
              "Cannot save conversion " + details,
              HttpStatus.INTERNAL_SERVER_ERROR,
              "unknown",
              null,
              null,
              null);
      exception.initCause(cause);
      return exception;
    }
  }

  interface FixerIO {
    static HttpStatusCodeException ratesRequestFailed(Exception cause) {
      HttpStatusCodeException exception =
          new HttpServerErrorException(
              "Cannot get rates from fixer.io",
              HttpStatus.SERVICE_UNAVAILABLE,
              "service-unavailable",
              null,
              null,
              null);
      exception.initCause(cause);
      return exception;
    }
  }

  interface Rate {
    static HttpStatusCodeException cannotReadRate(Exception cause, String source, String target) {
      HttpStatusCodeException exception =
          new HttpServerErrorException(
              "Cannot read rate from " + source + " to " + target,
              HttpStatus.INTERNAL_SERVER_ERROR,
              "unknown",
              null,
              null,
              null);
      exception.initCause(cause);
      return exception;
    }
  }
}
