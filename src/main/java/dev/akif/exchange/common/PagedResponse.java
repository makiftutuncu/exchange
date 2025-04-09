package dev.akif.exchange.common;

import java.util.List;

public record PagedResponse<A>(
    List<A> data, int page, int totalPages, int pageSize, long totalSize) {}
