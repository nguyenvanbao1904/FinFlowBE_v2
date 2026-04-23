package com.finflow.backend.identity.presentation.mapper;

import com.finflow.backend.identity.application.dto.AuthOutput;
import com.finflow.backend.identity.application.dto.CheckUserExistenceOutput;
import com.finflow.backend.identity.application.dto.UserOutput;
import com.finflow.backend.identity.application.dto.VerifyOtpOutput;
import com.finflow.backend.identity.presentation.response.AuthResponse;
import com.finflow.backend.identity.presentation.response.CheckUserExistenceResponse;
import com.finflow.backend.identity.presentation.response.UserResponse;
import com.finflow.backend.identity.presentation.response.VerifyOtpResponse;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface IdentityPresentationMapper {

    AuthResponse toAuthResponse(AuthOutput output);

    UserResponse toUserResponse(UserOutput output);

    VerifyOtpResponse toVerifyOtpResponse(VerifyOtpOutput output);

    CheckUserExistenceResponse toCheckUserExistenceResponse(CheckUserExistenceOutput output);
}
