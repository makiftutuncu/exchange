package dev.akif.exchange.rate;

import dev.akif.exchange.rate.dto.RateRequest;
import dev.akif.exchange.rate.dto.RateResponse;
import e.java.EOr;

public interface RateService {
    EOr<RateResponse> rate(RateRequest request);
}
