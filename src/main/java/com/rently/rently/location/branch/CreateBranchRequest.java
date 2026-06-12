package com.rently.rently.location.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBranchRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    private String city;

    private String address;
    private String phone;
}
