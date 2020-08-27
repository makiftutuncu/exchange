package dev.akif.exchange.provider;

import java.util.Map;

import e.java.EOr;

public interface RateProvider {
    EOr<Map<String, Double>> rates();
}
