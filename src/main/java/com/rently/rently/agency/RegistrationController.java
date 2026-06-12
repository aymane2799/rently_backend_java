package com.rently.rently.agency;

import com.rently.rently.agency.dto.AgencyRegistrationResponse;
import com.rently.rently.agency.dto.SubmitRegistrationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agencies")
@RequiredArgsConstructor
public class RegistrationController {

    private final AgencyRegistrationService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AgencyRegistrationResponse submit(@RequestBody @Valid SubmitRegistrationRequest request) {
        return service.submit(request);
    }
}
