package fitcubes.security;

import fitcubes.dto.user.UserDto;
import fitcubes.dto.user.UserLoginRequestDto;
import fitcubes.dto.user.UserLoginResponseDto;
import fitcubes.dto.user.UserRegistrationRequestDto;
import fitcubes.exception.RegistrationException;
import fitcubes.mapper.UserMapper;
import fitcubes.model.Role;
import fitcubes.model.RoleName;
import fitcubes.model.User;
import fitcubes.repository.RoleRepository;
import fitcubes.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
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
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    public UserLoginResponseDto login(UserLoginRequestDto requestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.email(), requestDto.password()));

        String token = jwtUtil.generateToken(authentication.getName());
        return new UserLoginResponseDto(token);
    }

    public UserDto register(UserRegistrationRequestDto requestDto) {
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

        return userMapper.toDto(savedUser);
    }

    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header format");
        }

        String token = authHeader.substring(7);
        long remainingTimeMs = jwtUtil.getRemainingExpirationTime(token);
        tokenBlacklistService.blacklistToken(token, remainingTimeMs);
    }
}
