package dev.akif.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import dev.akif.exchange.common.Errors;
import dev.akif.exchange.provider.TimeProvider;
import e.java.E;
import e.java.EException;

@RestControllerAdvice
public class ErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private final TimeProvider timeProvider;

    @Autowired
    public ErrorHandler(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handle(NoHandlerFoundException exception) {
        return handle(Errors.notFound.data("method", exception.getHttpMethod()).data("url", exception.getRequestURL()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception exception) {
        logger.error("Caught an exception", exception);

        E e = exception instanceof EException ?
            ((EException) exception).e :
            Errors.internalServerError.message("An unknown error occurred").cause(E.fromThrowable(exception));

        return handle(e);
    }

    private ResponseEntity<String> handle(E e) {
        return ResponseEntity.status(e.code().orElse(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(e.time(timeProvider.now()).toString());
    }
}
