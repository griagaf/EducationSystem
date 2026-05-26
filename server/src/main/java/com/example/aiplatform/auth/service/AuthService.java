package com.example.aiplatform.auth.service;

import com.example.aiplatform.auth.dto.AuthResponse;
import com.example.aiplatform.auth.dto.CurrentUserResponse;
import com.example.aiplatform.auth.dto.LoginRequest;
import com.example.aiplatform.auth.dto.RegisterRequest;
import com.example.aiplatform.common.exception.EmailAlreadyExistsException;
import com.example.aiplatform.common.exception.InvalidCredentialsException;
import com.example.aiplatform.common.exception.ResourceNotFoundException;
import com.example.aiplatform.user.entity.User;
import com.example.aiplatform.user.entity.UserProfile;
import com.example.aiplatform.user.repository.UserProfileRepository;
import com.example.aiplatform.user.repository.UserRepository;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("User with this email already exists");
        }

        User user = userRepository.save(new User(email, passwordEncoder.encode(request.password())));
        UserProfile profile = userProfileRepository.save(new UserProfile(user, request.displayName().trim()));
        return authResponse(user, profile);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .filter(User::isEnabled)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        return authResponse(user, profile);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        return userResponse(user, profile);
    }

    private AuthResponse authResponse(User user, UserProfile profile) {
        return new AuthResponse(jwtService.generateToken(user.getId(), user.getEmail()), userResponse(user, profile));
    }

    private CurrentUserResponse userResponse(User user, UserProfile profile) {
        return new CurrentUserResponse(user.getId(), user.getEmail(), profile.getDisplayName());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
