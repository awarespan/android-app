package com.awarespan.guardian.domain.usecase;

import com.awarespan.guardian.domain.model.User;
import com.awarespan.guardian.domain.repository.AuthRepository;

import javax.inject.Inject;

public class LoginUseCase {

    private final AuthRepository authRepository;

    @Inject
    public LoginUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public User execute(String username, String password) {
        return authRepository.login(username, password);
    }
}
