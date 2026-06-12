package com.rently.rently.reservation.customer;

import com.rently.rently.shared.mappers.CreateMapper;
import com.rently.rently.shared.mappers.PatchMapper;
import com.rently.rently.shared.mappers.ResponseMapper;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper implements
        ResponseMapper<Customer, CustomerResponse>,
        CreateMapper<Customer, CreateCustomerRequest, Object>,
        PatchMapper<Customer, UpdateCustomerRequest, Object> {

    @Override
    public Customer toEntity(CreateCustomerRequest request, Object ctx) {
        return Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .idType(request.getIdType())
                .idNumber(request.getIdNumber())
                .driverLicenseCode(request.getDriverLicenseCode())
                .address(request.getAddress())
                .build();
    }

    @Override
    public void patchEntity(Customer entity, UpdateCustomerRequest request, Object ctx) {
        if (request.getFirstName() != null) entity.setFirstName(request.getFirstName());
        if (request.getLastName() != null) entity.setLastName(request.getLastName());
        if (request.getPhone() != null) entity.setPhone(request.getPhone());
        if (request.getEmail() != null) entity.setEmail(request.getEmail());
        if (request.getIdType() != null) entity.setIdType(request.getIdType());
        if (request.getIdNumber() != null) entity.setIdNumber(request.getIdNumber());
        if (request.getDriverLicenseCode() != null) entity.setDriverLicenseCode(request.getDriverLicenseCode());
        if (request.getAddress() != null) entity.setAddress(request.getAddress());
    }

    @Override
    public CustomerResponse toResponse(Customer entity) {
        return CustomerResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .idType(entity.getIdType())
                .idNumber(entity.getIdNumber())
                .driverLicenseCode(entity.getDriverLicenseCode())
                .address(entity.getAddress())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
