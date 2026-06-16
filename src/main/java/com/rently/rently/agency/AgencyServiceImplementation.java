package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyBrandingResponse;
import com.rently.rently.agency.dto.AgencyResponse;
import com.rently.rently.agency.dto.UpdateAgencyBrandingRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgencyServiceImplementation implements AgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;

    @Override
    public AgencyResponse get(String id) {
        return agencyMapper.toResponse(findById(id));
    }

    @Override
    public List<AgencyResponse> getAll(AgencyStatus status) {
        List<Agency> agencies = status != null
                ? agencyRepository.findAllByStatus(status)
                : agencyRepository.findAll();
        return agencies.stream().map(agencyMapper::toResponse).toList();
    }

    @Override
    public AgencyResponse block(String id) {
        Agency agency = findById(id);
        if (agency.getStatus() == AgencyStatus.BLOCKED) {
            throw new IllegalStateException("Agency is already blocked");
        }
        agency.setStatus(AgencyStatus.BLOCKED);
        return agencyMapper.toResponse(agencyRepository.save(agency));
    }

    @Override
    public AgencyResponse unblock(String id) {
        Agency agency = findById(id);
        if (agency.getStatus() == AgencyStatus.APPROVED) {
            throw new IllegalStateException("Agency is already active");
        }
        agency.setStatus(AgencyStatus.APPROVED);
        return agencyMapper.toResponse(agencyRepository.save(agency));
    }

    @Override
    public AgencyBrandingResponse getBranding(String agencySlug) {
        return agencyMapper.toBrandingResponse(agencyRepository.findBySlug(agencySlug)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found: " + agencySlug)));
    }

    @Override
    public void updateBranding(String agencySlug, UpdateAgencyBrandingRequest request) {
        Agency agency = agencyRepository.findBySlug(agencySlug)
                .orElseThrow(() -> new EntityNotFoundException("Agency not found: " + agencySlug));
        if (request.getTagline() != null) agency.setTagline(request.getTagline());
        if (request.getPrimaryColor() != null) agency.setPrimaryColor(request.getPrimaryColor());
        if (request.getSecondaryColor() != null) agency.setSecondaryColor(request.getSecondaryColor());
        if (request.getDarkPrimaryColor() != null) agency.setDarkPrimaryColor(request.getDarkPrimaryColor());
        if (request.getDarkSecondaryColor() != null) agency.setDarkSecondaryColor(request.getDarkSecondaryColor());
        if (request.getMetaTitle() != null) agency.setMetaTitle(request.getMetaTitle());
        if (request.getMetaDescription() != null) agency.setMetaDescription(request.getMetaDescription());
        if (request.getMetaKeywords() != null) agency.setMetaKeywords(request.getMetaKeywords());
        if (request.getOgImageUrl() != null) agency.setOgImageUrl(request.getOgImageUrl());
        agencyRepository.save(agency);
    }

    private Agency findById(String id) {
        return agencyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agency with id " + id + " not found"));
    }
}
