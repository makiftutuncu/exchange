package dev.akif.exchange.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.function.ThrowingSupplier;

public abstract class Controller {
  public <A> ResponseEntity<A> respond(ThrowingSupplier<A> maybeA) {
    return respond(maybeA, HttpStatus.OK);
  }

  public <A> ResponseEntity<A> respond(ThrowingSupplier<A> maybeA, HttpStatus status) {
    A a = maybeA.get();
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(a);
  }
}
