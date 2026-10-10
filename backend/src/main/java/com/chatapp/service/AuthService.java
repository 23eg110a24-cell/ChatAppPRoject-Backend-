package com.chatapp.service;

import com.chatapp.domain.AvatarColors;
import com.chatapp.domain.User;
import com.chatapp.dto.AuthResponse;
import com.chatapp.dto.LoginRequest;
import com.chatapp.dto.RegisterRequest;
import com.chatapp.error.ApiException;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class AuthService {

    private static final String BAD_CREDENTIALS = "Invalid username or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public AuthResponse register(RegisterRequest request) {
        String username = request.username().strip();
        String email = request.email().strip();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw ApiException.conflict("That username is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("That email is already registered");
        }

        String displayName = (request.displayName() == null || request.displayName().isBlank())
                ? username
                : request.displayName().strip();

        User user = new User(
                username,
                email,
                passwordEncoder.encode(request.password()),
                displayName,
                AvatarColors.forUsername(username));

        return issue(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.usernameOrEmail().strip();

        User user = userRepository.findByUsernameIgnoreCase(identifier)
                .or(() -> userRepository.findByEmailIgnoreCase(identifier))
                .orElseThrow(() -> ApiException.unauthorized(BAD_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized(BAD_CREDENTIALS);
        }

        user.setLastSeenAt(Instant.now());
        return issue(user);
    }

    private AuthResponse issue(User user) {
        return new AuthResponse(
                jwtService.issueToken(user),
                jwtService.getExpirationSeconds(),
                userService.toDto(user));
    }
}