package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.port.in.UpdateProfilePort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.application.dto.UserOutput;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.application.command.UpdateProfileCommand;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.identity.application.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateProfileUseCase implements UpdateProfilePort {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserOutput execute(UpdateProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        if (command.firstName() != null) {
            user.setFirstName(command.firstName());
        }
        if (command.lastName() != null) {
            user.setLastName(command.lastName());
        }
        if (command.dob() != null) {
            user.setDob(command.dob());
        }

        User savedUser = userRepository.save(user);

        UserOutput response = userMapper.toUserOutput(savedUser);
        return UserOutput.builder()
                .id(response.id())
                .username(response.username())
                .email(response.email())
                .firstName(response.firstName())
                .lastName(response.lastName())
                .dob(response.dob())
                .isBiometricEnabled(response.isBiometricEnabled())
                .hasPassword(response.hasPassword())
                .roles(savedUser.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()))
                .build();
    }
}
