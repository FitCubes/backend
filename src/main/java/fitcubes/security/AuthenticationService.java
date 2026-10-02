package fitcubes.security;

import fitcubes.dto.user.UserLoginRequestDto;
import fitcubes.dto.user.UserLoginResponseDto;
import fitcubes.dto.user.UserRegistrationRequestDto;
import fitcubes.dto.user.UserSummaryDto;
import fitcubes.exception.EntityNotFoundException;
import fitcubes.exception.RegistrationException;
import fitcubes.mapper.UserMapper;
import fitcubes.model.user.PasswordResetToken;
import fitcubes.model.user.Role;
import fitcubes.model.user.RoleName;
import fitcubes.model.user.User;
import fitcubes.repository.PasswordResetTokenRepository;
import fitcubes.repository.RoleRepository;
import fitcubes.repository.UserRepository;
import fitcubes.service.email.EmailService;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    @Value("${app.frontend-url}")
    private String frontendUrl;

    public UserLoginResponseDto login(UserLoginRequestDto requestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.email(), requestDto.password()));

        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(
                () -> new EntityNotFoundException(
                        "User with email: " + authentication.getName() + " not found"));

        String token = jwtUtil.generateToken(authentication.getName());
        return new UserLoginResponseDto(token, toSummaryDto(user));
    }

    public UserLoginResponseDto register(UserRegistrationRequestDto requestDto) {
        String normalizedEmail = requestDto.email().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RegistrationException("Unable to register with the provided details.");
        }

        Role userRole = roleRepository.findByName(RoleName.USER).orElseThrow(
                () -> new RegistrationException("Role: " + RoleName.USER + " not found"));

        User user = userMapper.toEntity(requestDto);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        user.setRoles(Set.of(userRole));
        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(savedUser.getEmail());
        return new UserLoginResponseDto(token, toSummaryDto(savedUser));
    }

    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header format");
        }

        String token = authHeader.substring(7);
        long remainingTimeMs = jwtUtil.getRemainingExpirationTime(token);
        tokenBlacklistService.blacklistToken(token, remainingTimeMs);
    }

    private UserSummaryDto toSummaryDto(User user) {
        return new UserSummaryDto(user.getId(), user.getEmail(), buildName(user));
    }

    private String buildName(User user) {
        if (user.getFirstName() == null && user.getLastName() == null) {
            return null;
        }
        return (user.getFirstName() != null ? user.getFirstName() : "")
                + (user.getLastName() != null ? " " + user.getLastName() : "");
    }

    public void forgotPassword(String email) {
        String normalizedEmail = email.toLowerCase();
        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            String rawToken = generateSecureToken();
            String tokenHash = hashToken(rawToken);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUserId(user.getId());
            resetToken.setTokenHash(tokenHash);
            resetToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
            resetToken.setUsed(false);
            resetToken.setCreatedAt(Instant.now());
            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User with id: " + resetToken.getUserId() + " not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        Cache usersCache = cacheManager.getCache("users");
        if (usersCache != null) {
            usersCache.evict(user.getEmail());
        }

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
