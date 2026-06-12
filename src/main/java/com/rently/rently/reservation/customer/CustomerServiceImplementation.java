package com.rently.rently.reservation.customer;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImplementation implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Override
    public List<CustomerResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse get(String id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer with id " + id + " not found!"));
        return mapper.toResponse(customer);
    }

    @Override
    public CustomerResponse create(CreateCustomerRequest request) {
        if (repository.existsByIdTypeAndIdNumber(request.getIdType(), request.getIdNumber())) {
            throw new EntityExistsException(
                    "Customer with " + request.getIdType() + " number '" + request.getIdNumber() + "' already exists!"
            );
        }
        Customer customer = mapper.toEntity(request, null);
        return mapper.toResponse(repository.save(customer));
    }

    @Override
    public void update(String id, UpdateCustomerRequest request) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer with id " + id + " not found!"));

        if (request.getIdType() != null || request.getIdNumber() != null) {
            IdType newIdType = request.getIdType() != null ? request.getIdType() : customer.getIdType();
            String newIdNumber = request.getIdNumber() != null ? request.getIdNumber() : customer.getIdNumber();
            repository.findByIdTypeAndIdNumber(newIdType, newIdNumber).ifPresent(existing -> {
                if (!existing.getId().equals(customer.getId())) {
                    throw new EntityExistsException(
                            "Customer with " + newIdType + " number '" + newIdNumber + "' already exists!"
                    );
                }
            });
        }

        mapper.patchEntity(customer, request, null);
        repository.save(customer);
    }
}
