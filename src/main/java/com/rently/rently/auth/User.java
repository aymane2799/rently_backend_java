package com.rently.rently.auth;

import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends Auditable {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "agency_slug")
    private String agencySlug;

    @Column(name = "branch_id")
    private String branchId;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
