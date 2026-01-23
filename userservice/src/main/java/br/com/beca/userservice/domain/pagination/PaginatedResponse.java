package br.com.beca.userservice.domain.pagination;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaginatedResponse<T> {

    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    public PaginatedResponse() {
    }

    public PaginatedResponse(List<T> data,
                             int currentPage,
                             int totalPages,
                             long totalItems,
                             int pageSize,
                             boolean hasNext,
                             boolean hasPrevious) {
        this.data = data;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    protected <U> List<U> getConvertedContent(Function<? super T, ? extends U> converter) {
        Objects.requireNonNull(converter);
        Stream<T> stream = this.data.stream();
        return stream.map(converter).collect(Collectors.toList());
    }

    public <U> PaginatedResponse<U> map(Function<? super T, ? extends U> converter) {
        return new PaginatedResponse<>(
                this.getConvertedContent(converter),
                this.currentPage,
                this.totalPages,
                this.totalItems,
                this.pageSize,
                this.hasNext,
                this.hasPrevious
        );
    }

}