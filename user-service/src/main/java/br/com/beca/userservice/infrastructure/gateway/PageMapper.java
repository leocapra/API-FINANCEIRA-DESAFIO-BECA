package br.com.beca.userservice.infrastructure.gateway;

import br.com.beca.userservice.domain.pagination.PageDataDomain;
import br.com.beca.userservice.domain.pagination.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageMapper {

    public <T>PaginatedResponse<T> converter(Page<T> page) {
        PaginatedResponse<T> response = new PaginatedResponse<>();
        response.setData(page.getContent());
        response.setCurrentPage(page.getNumber());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        response.setPageSize(page.getSize());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        return response;
    }

    public PageDataDomain pageableToPageDataDomain(Pageable pageable) {
        return new PageDataDomain(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                new PageSortMapper().sortToDomain(pageable.getSort().get().toList())
        );
    }


}