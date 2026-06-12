package com.rently.rently.location.hub.hydration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubHydrator {

    private final HubHydrationResolver resolver;

    public HubHydrationContext hydrate(String branchId) {
        return HubHydrationContext.builder()
                .branch(resolver.resolveBranch(branchId))
                .build();
    }

    public HubHydrationContext emptyContext() {
        return HubHydrationContext.builder().build();
    }
}
