package com.rently.rently.location.branch;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateBranchRequest {

    @Size(max = 150)
    private String name;

    private String city;
    private String address;
    private String phone;
}
