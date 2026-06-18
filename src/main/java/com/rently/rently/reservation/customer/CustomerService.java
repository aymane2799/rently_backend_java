package com.rently.rently.reservation.customer;

import com.rently.rently.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    List<CustomerResponse> getAll();
    PagedResponse<CustomerResponse> getAll(IdType idType, String search, Pageable pageable);
    List<CustomerOptionResponse> getOptions(String search);
    CustomerResponse get(String id);
    CustomerResponse create(CreateCustomerRequest request);
    void update(String id, UpdateCustomerRequest request);
}
