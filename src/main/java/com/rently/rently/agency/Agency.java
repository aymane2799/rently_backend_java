package com.rently.rently.agency;

import com.rently.rently.shared.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "agencies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agency extends Auditable {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "rc_number", nullable = false, unique = true)
    private String rcNumber;

    @Column(name = "ice_number", nullable = false, unique = true)
    private String iceNumber;

    @Column(name = "if_number", unique = true)
    private String ifNumber;

    @Column(name = "patent", unique = true)
    private String patent;

    @Column(name = "owner_first_name", nullable = false)
    private String ownerFirstName;

    @Column(name = "owner_last_name", nullable = false)
    private String ownerLastName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "address")
    private String address;

    @Column(name = "website")
    private String website;

    @Column(name = "logo")
    private String logo;

    @Column(name = "cover_image")
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AgencyStatus status = AgencyStatus.APPROVED;

    @Column(name = "approved_at", nullable = false)
    private Instant approvedAt;

    @Column(name = "plan_id")
    private String planId;
}
