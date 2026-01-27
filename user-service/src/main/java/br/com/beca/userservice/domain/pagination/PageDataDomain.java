package br.com.beca.userservice.domain.pagination;

import java.util.List;

public class PageDataDomain {
    private Integer page;
    private Integer size;
    private List<SortField> sort;

    public PageDataDomain(Integer page, Integer size, List<SortField> sortBy) {
        sort = sortBy;
        setPage(page);
        setSize(size);
    }

    private void setSize(Integer size) {
        if (size == null || size <= 0)
            throw new IllegalArgumentException("Tamanho da página deve ser maior que 0.");
        this.size = size;
    }

    private void setPage(Integer page) {
        if (page == null || page < 0)
            throw new IllegalArgumentException("Índice da página deve ser maior ou igual a 0.");
        this.page = page;
    }


    public record SortField(
            String field,
            String order
    ) {
    }


    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }

    public List<SortField> getSort() {
        return sort;
    }
}