package dev.akif.exchange.provider.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FixerIOResponse(String source, Map<String, Double> rates) {}
