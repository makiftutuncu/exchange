package dev.akif.exchange.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import e.java.EOr;

public abstract class Controller {
    public <A> ResponseEntity<A> respond(EOr<A> maybeA) {
        return respond(maybeA, HttpStatus.OK);
    }

    public <A> ResponseEntity<A> respond(EOr<A> maybeA, HttpStatus status) {
        return maybeA.fold(
            e -> { throw e.toException(); },
            a -> ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(a)
        );
    }
}
