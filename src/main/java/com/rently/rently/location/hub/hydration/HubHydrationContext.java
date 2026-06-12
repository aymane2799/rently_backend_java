package com.rently.rently.location.hub.hydration;

import com.rently.rently.location.branch.Branch;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class HubHydrationContext {
    private Branch branch;
}
