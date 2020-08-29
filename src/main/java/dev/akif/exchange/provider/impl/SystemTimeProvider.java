package dev.akif.exchange.provider.impl;

import org.springframework.stereotype.Component;

import dev.akif.exchange.provider.TimeProvider;

@Component
public class SystemTimeProvider implements TimeProvider {
    @Override
    public long now() {
        return System.currentTimeMillis();
    }
}
