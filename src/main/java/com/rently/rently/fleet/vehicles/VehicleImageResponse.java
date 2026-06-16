package com.rently.rently.fleet.vehicles;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleImageResponse {
    private String id;
    private String imageUrl;
    private boolean isPrimary;
    private int displayOrder;
    private String altText;
}
