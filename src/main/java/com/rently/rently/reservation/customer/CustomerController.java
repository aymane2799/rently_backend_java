package com.rently.rently.reservation.customer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @GetMapping
    public List<CustomerResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@RequestBody @Valid CreateCustomerRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String id, @RequestBody @Valid UpdateCustomerRequest request) {
        service.update(id, request);
    }
}
