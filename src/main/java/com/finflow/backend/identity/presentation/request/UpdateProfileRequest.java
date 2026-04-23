package com.finflow.backend.identity.presentation.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @Size(min = 1, max = 50)
    private String firstName;
    @Size(min = 1, max = 50)
    private String lastName;
    @Past
    private LocalDate dob;
}
