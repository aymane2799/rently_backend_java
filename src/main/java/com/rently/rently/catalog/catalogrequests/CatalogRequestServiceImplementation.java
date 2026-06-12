package com.rently.rently.catalog.catalogrequests;

import com.rently.rently.catalog.brands.Brand;
import com.rently.rently.catalog.brands.BrandRepository;
import com.rently.rently.catalog.features.Feature;
import com.rently.rently.catalog.features.FeatureRepository;
import com.rently.rently.catalog.models.Model;
import com.rently.rently.catalog.models.ModelRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogRequestServiceImplementation implements CatalogRequestService {

    private final CatalogRequestRepository repository;
    private final BrandRepository brandRepository;
    private final ModelRepository modelRepository;
    private final FeatureRepository featureRepository;

    @Override
    public CatalogRequestResponse submit(SubmitCatalogRequestRequest request, String agencySlug) {
        validateTypeSpecificFields(request);

        Brand proposedBrand = null;
        if (request.getProposedBrandId() != null) {
            proposedBrand = brandRepository.findById(request.getProposedBrandId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Brand not found: " + request.getProposedBrandId()));
        }

        CatalogRequest entity = CatalogRequest.builder()
                .agencySlug(agencySlug)
                .type(request.getType())
                .proposedName(request.getProposedName())
                .proposedBrand(proposedBrand)
                .proposedBrandName(request.getProposedBrandName())
                .proposedCategory(request.getProposedCategory())
                .proposedIcon(request.getProposedIcon())
                .proposedDescription(request.getProposedDescription())
                .notes(request.getNotes())
                .submittedAt(Instant.now())
                .build();

        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void approve(String id, String adminUserId) {
        CatalogRequest req = findPending(id);

        String resolvedEntityId = switch (req.getType()) {
            case BRAND -> approveBrand(req);
            case MODEL -> approveModel(req);
            case FEATURE -> approveFeature(req);
        };

        req.setStatus(CatalogRequestStatus.APPROVED);
        req.setReviewedAt(Instant.now());
        req.setReviewedBy(adminUserId);
        req.setResolvedEntityId(resolvedEntityId);
        repository.save(req);
    }

    @Override
    public void reject(String id, String rejectionReason, String adminUserId) {
        CatalogRequest req = findPending(id);
        req.setStatus(CatalogRequestStatus.REJECTED);
        req.setReviewedAt(Instant.now());
        req.setReviewedBy(adminUserId);
        req.setRejectionReason(rejectionReason);
        repository.save(req);
    }

    @Override
    public List<CatalogRequestResponse> getForAgency(String agencySlug) {
        return repository.findByAgencySlug(agencySlug)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CatalogRequestResponse> getAll(CatalogRequestType type, CatalogRequestStatus status) {
        List<CatalogRequest> results;
        if (type != null && status != null) {
            results = repository.findAllByTypeAndStatus(type, status);
        } else if (type != null) {
            results = repository.findAllByType(type);
        } else if (status != null) {
            results = repository.findAllByStatus(status);
        } else {
            results = repository.findAll();
        }
        return results.stream().map(this::toResponse).toList();
    }

    private void validateTypeSpecificFields(SubmitCatalogRequestRequest request) {
        if (request.getType() == CatalogRequestType.MODEL) {
            if (request.getProposedCategory() == null) {
                throw new IllegalArgumentException("proposedCategory is required for MODEL requests");
            }
            if (request.getProposedBrandId() == null && request.getProposedBrandName() == null) {
                throw new IllegalArgumentException(
                        "Either proposedBrandId or proposedBrandName is required for MODEL requests");
            }
        }
    }

    private String approveBrand(CatalogRequest req) {
        Brand brand = Brand.builder()
                .name(req.getProposedName())
                .build();
        return brandRepository.save(brand).getId();
    }

    private String approveModel(CatalogRequest req) {
        Brand brand;
        if (req.getProposedBrand() != null) {
            brand = req.getProposedBrand();
        } else {
            brand = Brand.builder()
                    .name(req.getProposedBrandName())
                    .build();
            brand = brandRepository.save(brand);
        }

        Model model = Model.builder()
                .name(req.getProposedName())
                .category(req.getProposedCategory())
                .brand(brand)
                .build();
        return modelRepository.save(model).getId();
    }

    private String approveFeature(CatalogRequest req) {
        Feature feature = Feature.builder()
                .name(req.getProposedName())
                .icon(req.getProposedIcon())
                .description(req.getProposedDescription())
                .build();
        return featureRepository.save(feature).getId();
    }

    private CatalogRequest findPending(String id) {
        CatalogRequest req = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CatalogRequest not found: " + id));
        if (req.getStatus() != CatalogRequestStatus.PENDING) {
            throw new IllegalStateException("CatalogRequest is already " + req.getStatus());
        }
        return req;
    }

    private CatalogRequestResponse toResponse(CatalogRequest entity) {
        return CatalogRequestResponse.builder()
                .id(entity.getId())
                .agencySlug(entity.getAgencySlug())
                .type(entity.getType())
                .status(entity.getStatus())
                .proposedName(entity.getProposedName())
                .proposedBrandId(entity.getProposedBrand() != null ? entity.getProposedBrand().getId() : null)
                .proposedBrandName(entity.getProposedBrandName())
                .proposedCategory(entity.getProposedCategory())
                .proposedIcon(entity.getProposedIcon())
                .proposedDescription(entity.getProposedDescription())
                .notes(entity.getNotes())
                .rejectionReason(entity.getRejectionReason())
                .submittedAt(entity.getSubmittedAt())
                .reviewedAt(entity.getReviewedAt())
                .reviewedBy(entity.getReviewedBy())
                .resolvedEntityId(entity.getResolvedEntityId())
                .build();
    }
}
