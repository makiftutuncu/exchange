package dev.akif.exchange.common;

import org.springframework.http.HttpStatus;

import e.java.E;

public interface Errors {
    E badRequest          = E.fromCode(HttpStatus.BAD_REQUEST.value()).name("invalid-input");
    E notFound            = E.fromCode(HttpStatus.NOT_FOUND.value()).name("not-found");
    E internalServerError = E.fromCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).name("unknown");
    E serviceUnavailable  = E.fromCode(HttpStatus.SERVICE_UNAVAILABLE.value()).name("service-unavailable");

    interface Common {
        E invalidCurrency = badRequest.message("Currency is invalid!");
    }

    interface FixerIO {
        E ratesRequestFailed = serviceUnavailable.message("Cannot get rates from fixer.io!");
        E parsingRatesFailed = serviceUnavailable.message("Cannot parse rates from fixer.io!");
    }

    interface Rate {
        E cannotReadRate = internalServerError.name("database").message("Cannot read rate!");
    }
}
