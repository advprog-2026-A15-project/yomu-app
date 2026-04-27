package id.ac.ui.cs.advprog.yomu.auth;

import id.ac.ui.cs.advprog.yomu.auth.internal.repository.UserRepository;
import id.ac.ui.cs.advprog.yomu.auth.internal.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public Optional<UserDto> getUserById(UUID id) {
        return userRepository.findById(id).map(user -> UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .displayName(user.getDisplayName())
                .role(user.getRole().name())
                .build());
    }

    public boolean isTokenValid(String token) {
        try {
            String username = jwtService.extractUsername(token);
            return userRepository.findByUsernameOrEmail(username)
                    .map(user -> jwtService.isTokenValid(token, user))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    public Optional<UUID> getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof id.ac.ui.cs.advprog.yomu.auth.internal.model.User user)) {
            return Optional.empty();
        }

        return Optional.ofNullable(user.getId());
    }
}
