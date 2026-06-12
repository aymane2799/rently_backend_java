package com.rently.rently.multitenancy;

import com.rently.rently.agency.AgencyRepository;
import com.rently.rently.agency.AgencyStatus;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
@RequiredArgsConstructor
public class TenantFilter implements Filter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    private final AgencyRepository agencyRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = httpRequest.getHeader(TENANT_HEADER);
        try {
            if (tenantId != null && !tenantId.isBlank()) {
                boolean blocked = agencyRepository.findBySlug(tenantId)
                        .map(agency -> agency.getStatus() == AgencyStatus.BLOCKED)
                        .orElse(false);
                if (blocked) {
                    HttpServletResponse httpResponse = (HttpServletResponse) response;
                    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    httpResponse.getWriter().write("{\"message\":\"Agency is blocked\",\"status\":403}");
                    return;
                }
                TenantContext.setTenantId(tenantId);
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
