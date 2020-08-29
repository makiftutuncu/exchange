package dev.akif.exchange.rate.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.akif.exchange.common.Controller;
import dev.akif.exchange.rate.RateService;
import dev.akif.exchange.rate.dto.RateRequest;
import dev.akif.exchange.rate.dto.RateResponse;
import e.java.EOr;

@RestController
@RequestMapping("/rates")
public class RateController extends Controller {
    private final RateService rateService;

    @Autowired
    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @GetMapping
    public ResponseEntity<RateResponse> rates(@RequestParam("source") String source,
                                              @RequestParam("target") String target) {
        EOr<RateResponse> response = RateRequest.of(source, target).flatMap(rateService::rate);

        return respond(response);
    }
}
