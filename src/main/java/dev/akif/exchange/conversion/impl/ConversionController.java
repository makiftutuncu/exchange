package dev.akif.exchange.conversion.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/{id}")
    public ResponseEntity<ConversionResponse> get(@PathVariable("id") long id) {
        EOr<ConversionResponse> response = conversionService.get(id);

        return respond(response);
    }

    @GetMapping
    public ResponseEntity<List<ConversionResponse>> list(
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(value = "page", required = false, defaultValue = "1") int page,
        @RequestParam(value = "size", required = false, defaultValue = "0") int size,
        @RequestParam(value = "newestFirst", required = false, defaultValue = "true") boolean newestFirst
    ) {
        EOr<List<ConversionResponse>> response = conversionService.list(from, to, page, size, newestFirst);

        return respond(response);
    }
}
