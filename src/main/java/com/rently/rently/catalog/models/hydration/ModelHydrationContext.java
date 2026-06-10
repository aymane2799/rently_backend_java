package com.rently.rently.catalog.models.hydration;


import com.rently.rently.catalog.brands.Brand;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ModelHydrationContext{
        Brand brand;
}
