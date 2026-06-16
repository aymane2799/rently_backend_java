package com.rently.rently.reservation.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientAccountRepository extends JpaRepository<ClientAccount, String> {
    Optional<ClientAccount> findByEmail(String email);
    boolean existsByEmail(String email);
}
