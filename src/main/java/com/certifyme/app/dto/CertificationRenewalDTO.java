package com.certifyme.app.dto;

import com.certifyme.app.model.RenewalAction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationRenewalDTO {
    
    @NotNull(message = "Action is required")
    private RenewalAction action;
    
    private String remarks;
}
