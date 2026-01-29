package com.finflow.backend.modules.identity.usecase;

import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import com.finflow.backend.modules.identity.dto.CheckUserExistenceRequest;
import com.finflow.backend.modules.identity.dto.CheckUserExistenceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckUserExistenceUseCase {

    private final UserRepository userRepository;

    public CheckUserExistenceResponse execute(CheckUserExistenceRequest request) {
        boolean exists = userRepository.existsByEmail(request.getEmail());
        return new CheckUserExistenceResponse(exists);
    }
}
