package dev.akif.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import dev.akif.exchange.common.Errors;
import e.java.E;
import e.java.EException;

@ControllerAdvice
public class ErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handle(NoHandlerFoundException exception) {
        logger.info("No handler found!");

        return handle(Errors.notFound.data("method", exception.getHttpMethod()).data("url", exception.getRequestURL()));
    }

    @ExceptionHandler(EException.class)
    public ResponseEntity<String> handle(EException exception) {
        logger.error("Caught an handled error!", exception);

        return handle(exception.e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception exception) {
        logger.error("Caught an unhandled exception!", exception);

        return handle(Errors.internalServerError.message("An unknown error occurred!").cause(E.fromThrowable(exception)));
    }

    private ResponseEntity<String> handle(E e) {
        return ResponseEntity.status(e.code().orElse(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(e.toString());
    }
}
