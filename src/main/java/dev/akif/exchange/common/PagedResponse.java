package dev.akif.exchange.common;

import java.util.List;
import java.util.StringJoiner;

public class PagedResponse<A> {
    public final List<A> data;
    public final int page;
    public final int totalPages;
    public final int pageSize;
    public final long totalSize;

    public PagedResponse(List<A> data, int page, int totalPages, int pageSize, long totalSize) {
        this.data = data;
        this.page = page;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.totalSize = totalSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PagedResponse)) return false;

        PagedResponse<?> that = (PagedResponse<?>) o;

        if (this.page != that.page) return false;
        if (this.totalPages != that.totalPages) return false;
        if (this.pageSize != that.pageSize) return false;
        if (this.totalSize != that.totalSize) return false;

        return this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = data.hashCode();
        result = 31 * result + page;
        result = 31 * result + totalPages;
        result = 31 * result + pageSize;
        result = 31 * result + Long.hashCode(totalSize);
        return result;
    }

    @Override
    public String toString() {
        StringJoiner dataJoiner = new StringJoiner(",", "[", "]");
        data.forEach(d -> dataJoiner.add(d.toString()));
        return new StringJoiner(",", "{", "}")
            .add("\"data\":" + dataJoiner.toString())
            .add("\"page\":" + page)
            .add("\"totalPages\":" + totalPages)
            .add("\"pageSize\":" + pageSize)
            .add("\"totalSize\":" + totalSize)
            .toString();
    }
}
