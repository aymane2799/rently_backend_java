package com.rently.rently.agency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
@RequiredArgsConstructor
public class SlugGenerator {

    private final AgencyRepository agencyRepository;

    public String generateUniqueSlug(String agencyName) {
        String base = toSlug(agencyName);
        if (!agencyRepository.existsBySlug(base)) {
            return base;
        }
        int suffix = 2;
        while (true) {
            String candidate = base + "-" + suffix;
            if (!agencyRepository.existsBySlug(candidate)) {
                return candidate;
            }
            suffix++;
        }
    }

    private String toSlug(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("[\\s-]+", "-");
    }
}
