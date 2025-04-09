package dev.akif.exchange.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ErrorHandler {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<String> handle(NoHandlerFoundException exception) {
    return handle(
        new HttpClientErrorException(
            exception.getHttpMethod() + " " + exception.getRequestURL(),
            HttpStatus.NOT_FOUND,
            "not-found",
            null,
            null,
            null));
  }

  @ExceptionHandler(HttpStatusCodeException.class)
  public ResponseEntity<String> handle(HttpStatusCodeException e) {
    return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handle(Exception exception) {
    logger.error("Caught an exception", exception);
    HttpStatusCodeException e =
        new HttpServerErrorException(
            "An unknown error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "unknown",
            null,
            null,
            null);
    e.initCause(exception);
    return handle(e);
  }
}
