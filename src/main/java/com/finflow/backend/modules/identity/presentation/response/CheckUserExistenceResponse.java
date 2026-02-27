package com.finflow.backend.modules.identity.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckUserExistenceResponse {
    private boolean exists;
    private Boolean isActive;  // null if user doesn't exist
    private Boolean hasPassword;  // null if user doesn't exist
    private Boolean isDeleted;  // true if deletedAt != null
}
