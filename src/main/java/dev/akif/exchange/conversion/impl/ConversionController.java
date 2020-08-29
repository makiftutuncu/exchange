package dev.akif.exchange.conversion.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.akif.exchange.common.Controller;
import dev.akif.exchange.common.CurrencyPair;
import dev.akif.exchange.conversion.ConversionService;
import dev.akif.exchange.conversion.dto.ConversionRequest;
import dev.akif.exchange.conversion.dto.ConversionResponse;
import e.java.EOr;

@RestController
@RequestMapping("/conversions")
public class ConversionController extends Controller {
    private final ConversionService conversionService;

    @Autowired
    public ConversionController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @PostMapping
    public ResponseEntity<ConversionResponse> convert(@RequestBody ConversionRequest request) {
        EOr<ConversionResponse> response =
            CurrencyPair.of(request.source, request.target)
                        .flatMap(pair -> conversionService.convert(pair, request.amount));

        return respond(response);
    }
}
