package com.arc.leakagetesting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeakageActionRequest {
    @NotBlank(message = "Action cannot be blank")
    private String action; // e.g. "Scrap", "Pending"
}
