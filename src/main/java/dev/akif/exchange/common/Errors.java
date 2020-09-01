package dev.akif.exchange.common;

import org.springframework.http.HttpStatus;

import e.java.E;

public interface Errors {
    E badRequest          = E.fromCode(HttpStatus.BAD_REQUEST.value()).name("bad-request");
    E notFound            = E.fromCode(HttpStatus.NOT_FOUND.value()).name("not-found");
    E internalServerError = E.fromCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).name("unknown");
    E serviceUnavailable  = E.fromCode(HttpStatus.SERVICE_UNAVAILABLE.value()).name("service-unavailable");

    interface Common {
        E invalidCurrency = badRequest.name("invalid-input").message("Currency is invalid");
    }

    interface Conversion {
        E conversionNotFound   = notFound.message("Cannot find conversion");
        E cannotReadConversion = internalServerError.name("database").message("Cannot read conversion");
        E cannotSaveConversion = internalServerError.name("database").message("Cannot save conversion");
    }

    interface FixerIO {
        E ratesRequestFailed = serviceUnavailable.message("Cannot get rates from fixer.io");
        E parsingRatesFailed = serviceUnavailable.message("Cannot parse rates from fixer.io");
    }

    interface Rate {
        E cannotReadRate = internalServerError.name("database").message("Cannot read rate");
    }
}
