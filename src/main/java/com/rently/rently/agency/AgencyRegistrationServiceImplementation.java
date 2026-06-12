package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyRegistrationResponse;
import com.rently.rently.agency.dto.SubmitRegistrationRequest;
import com.rently.rently.auth.User;
import com.rently.rently.auth.UserRepository;
import com.rently.rently.auth.UserRole;
import com.rently.rently.multitenancy.TenantSchemaProvisioner;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgencyRegistrationServiceImplementation implements AgencyRegistrationService {

    private final AgencyRegistrationRepository registrationRepository;
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SlugGenerator slugGenerator;
    private final TenantSchemaProvisioner tenantSchemaProvisioner;
    private final AgencyRegistrationMapper registrationMapper;

    @Override
    public AgencyRegistrationResponse submit(SubmitRegistrationRequest request) {
        if (registrationRepository.existsByRcNumber(request.getRcNumber())) {
            throw new EntityExistsException("A registration with RC number " + request.getRcNumber() + " already exists");
        }
        if (registrationRepository.existsByIceNumber(request.getIceNumber())) {
            throw new EntityExistsException("A registration with ICE number " + request.getIceNumber() + " already exists");
        }
        if (registrationRepository.existsByOwnerEmail(request.getOwnerEmail())) {
            throw new EntityExistsException("A registration with owner email " + request.getOwnerEmail() + " already exists");
        }

        AgencyRegistration registration = AgencyRegistration.builder()
                .agencyName(request.getAgencyName())
                .rcNumber(request.getRcNumber())
                .iceNumber(request.getIceNumber())
                .ifNumber(request.getIfNumber())
                .patent(request.getPatent())
                .city(request.getCity())
                .address(request.getAddress())
                .website(request.getWebsite())
                .ownerFirstName(request.getOwnerFirstName())
                .ownerLastName(request.getOwnerLastName())
                .ownerEmail(request.getOwnerEmail())
                .ownerPhone(request.getOwnerPhone())
                .submittedAt(Instant.now())
                .build();

        return registrationMapper.toResponse(registrationRepository.save(registration));
    }

    @Override
    public AgencyRegistrationResponse get(String id) {
        return registrationMapper.toResponse(findById(id));
    }

    @Override
    public List<AgencyRegistrationResponse> getAll(AgencyRegistrationStatus status) {
        List<AgencyRegistration> registrations = status != null
                ? registrationRepository.findAllByStatus(status)
                : registrationRepository.findAll();
        return registrations.stream().map(registrationMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public AgencyRegistrationResponse approve(String id, String reviewedBy) {
        AgencyRegistration registration = findById(id);

        if (registration.getStatus() != AgencyRegistrationStatus.PENDING) {
            throw new IllegalStateException("Registration is not in PENDING state");
        }

        String slug = slugGenerator.generateUniqueSlug(registration.getAgencyName());
        Instant now = Instant.now();

        Agency agency = Agency.builder()
                .name(registration.getAgencyName())
                .slug(slug)
                .rcNumber(registration.getRcNumber())
                .iceNumber(registration.getIceNumber())
                .ifNumber(registration.getIfNumber())
                .patent(registration.getPatent())
                .ownerFirstName(registration.getOwnerFirstName())
                .ownerLastName(registration.getOwnerLastName())
                .phone(registration.getOwnerPhone())
                .email(registration.getOwnerEmail())
                .city(registration.getCity())
                .address(registration.getAddress())
                .website(registration.getWebsite())
                .approvedAt(now)
                .build();

        agency = agencyRepository.save(agency);

        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        User owner = User.builder()
                .firstName(registration.getOwnerFirstName())
                .lastName(registration.getOwnerLastName())
                .email(registration.getOwnerEmail())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .role(UserRole.AGENCY_OWNER)
                .agencySlug(slug)
                .build();

        userRepository.save(owner);

        tenantSchemaProvisioner.provisionSchema(slug);

        registration.setStatus(AgencyRegistrationStatus.APPROVED);
        registration.setReviewedAt(now);
        registration.setReviewedBy(reviewedBy);
        registration.setResolvedAgencyId(agency.getId());

        return registrationMapper.toResponse(registrationRepository.save(registration));
    }

    @Override
    @Transactional
    public AgencyRegistrationResponse reject(String id, String rejectionReason, String reviewedBy) {
        AgencyRegistration registration = findById(id);

        if (registration.getStatus() != AgencyRegistrationStatus.PENDING) {
            throw new IllegalStateException("Registration is not in PENDING state");
        }

        registration.setStatus(AgencyRegistrationStatus.REJECTED);
        registration.setRejectionReason(rejectionReason);
        registration.setReviewedAt(Instant.now());
        registration.setReviewedBy(reviewedBy);

        return registrationMapper.toResponse(registrationRepository.save(registration));
    }

    private AgencyRegistration findById(String id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agency registration with id " + id + " not found"));
    }
}
