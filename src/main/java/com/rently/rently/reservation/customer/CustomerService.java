package com.rently.rently.reservation.customer;

import java.util.List;

public interface CustomerService {
    List<CustomerResponse> getAll();
    CustomerResponse get(String id);
    CustomerResponse create(CreateCustomerRequest request);
    void update(String id, UpdateCustomerRequest request);
}
