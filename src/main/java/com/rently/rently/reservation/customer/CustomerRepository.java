package com.rently.rently.reservation.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByIdTypeAndIdNumber(IdType idType, String idNumber);
    boolean existsByIdTypeAndIdNumber(IdType idType, String idNumber);
}
