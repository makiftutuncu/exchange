package dev.akif.exchange.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PagingHelperTest {
    @Test
    @DisplayName("getting from")
    void gettingFrom() {
        assertEquals(0L, PagingHelper.from(null));
        assertEquals(0L, PagingHelper.from(LocalDate.EPOCH));
        assertEquals(1598918400000L, PagingHelper.from(LocalDate.of(2020, 9, 1)));
    }

    @Test
    @DisplayName("getting to")
    void gettingTo() {
        assertEquals(Long.MAX_VALUE, PagingHelper.to(null));
        assertEquals(86400000L, PagingHelper.to(LocalDate.EPOCH));
        assertEquals(1599004800000L, PagingHelper.to(LocalDate.of(2020, 9, 1)));
    }

    @Test
    @DisplayName("getting page")
    void gettingPage() {
        assertEquals(0, PagingHelper.page(-1));
        assertEquals(0, PagingHelper.page(1));
        assertEquals(1, PagingHelper.page(2));
    }

    @Test
    @DisplayName("getting size")
    void gettingSize() {
        assertEquals(5, PagingHelper.size(-1, 5));
        assertEquals(1, PagingHelper.size(1, 5));
        assertEquals(2, PagingHelper.size(2, 5));
        assertEquals(5, PagingHelper.size(51, 5));
    }
}
