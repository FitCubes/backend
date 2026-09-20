package fitcubes.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fitcubes.dto.user.UserLoginRequestDto;
import fitcubes.dto.user.UserLoginResponseDto;
import fitcubes.dto.user.UserRegistrationRequestDto;
import fitcubes.exception.RegistrationException;
import fitcubes.mapper.UserMapper;
import fitcubes.model.user.Gender;
import fitcubes.model.user.Goal;
import fitcubes.model.user.Role;
import fitcubes.model.user.RoleName;
import fitcubes.model.user.User;
import fitcubes.repository.RoleRepository;
import fitcubes.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                jwtUtil,
                userRepository,
                passwordEncoder,
                roleRepository,
                userMapper,
                authenticationManager,
                tokenBlacklistService);
    }

    @Test
    @DisplayName("Should return token when credentials are valid")
    void login_ValidCredentials_ReturnsToken() {
        // given
        UserLoginRequestDto requestDto = new UserLoginRequestDto("john@example.com", "password123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "john@example.com", "password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("john@example.com")).thenReturn("generated-jwt-token");

        // when
        UserLoginResponseDto response = authenticationService.login(requestDto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("generated-jwt-token");
        assertThat(response.user().email()).isEqualTo("john@example.com");
        assertThat(response.user().name()).isEqualTo("John Doe");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("john@example.com");
    }

    @Test
    @DisplayName("Should pass correct credentials to authentication manager")
    void login_ValidCredentials_PassesCredentialsToAuthenticationManager() {
        // given
        UserLoginRequestDto requestDto = new UserLoginRequestDto("jane@example.com", "secret");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "jane@example.com", "secret");

        User user = new User();
        user.setId(2L);
        user.setEmail("jane@example.com");
        user.setFirstName("Jane");
        user.setLastName("Smith");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString())).thenReturn("token");

        // when
        authenticationService.login(requestDto);

        // then
        verify(authenticationManager).authenticate(eq(
                new UsernamePasswordAuthenticationToken("jane@example.com", "secret")));
    }

    @Test
    @DisplayName("Should propagate exception when authentication fails")
    void login_InvalidCredentials_ThrowsBadCredentialsException() {
        // given
        UserLoginRequestDto requestDto = new UserLoginRequestDto("john@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when / then
        assertThatThrownBy(() -> authenticationService.login(requestDto))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");

        verifyNoInteractions(jwtUtil);
    }

    @Test
    @DisplayName("Should register user successfully")
    void register_ValidRequest_RegistersUserSuccessfully() {
        // given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                "new@example.com",
                "rawPassword",
                "rawPassword",
                "John",
                "Doe",
                Gender.MALE,
                25,
                180,
                80.0,
                75.0,
                1.55,
                Goal.WEIGHT_LOSS);

        User userEntity = new User();
        Role userRole = new Role();
        userRole.setName(RoleName.USER);

        User savedUser = new User();
        savedUser.setEmail("new@example.com");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userMapper.toEntity(requestDto)).thenReturn(userEntity);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(userEntity)).thenReturn(savedUser);
        when(jwtUtil.generateToken("new@example.com")).thenReturn("generated-jwt-token");

        // when
        UserLoginResponseDto result = authenticationService.register(requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("generated-jwt-token");
        assertThat(userEntity.getRoles()).containsExactly(userRole);
        assertThat(userEntity.getPassword()).isEqualTo("encodedPassword");

        verify(userRepository).existsByEmail("new@example.com");
        verify(roleRepository).findByName(RoleName.USER);
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(userEntity);
        verify(jwtUtil).generateToken("new@example.com");
    }

    @Test
    @DisplayName("Should throw exception when email is already in use")
    void register_EmailAlreadyInUse_ThrowsRegistrationException() {
        // given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                "existing@example.com",
                "password",
                "password",
                "John",
                "Doe",
                Gender.FEMALE,
                25,
                170,
                65.0,
                60.0,
                1.375,
                Goal.MAINTENANCE);

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> authenticationService.register(requestDto))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("Unable to register with the provided details.");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper, roleRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should throw exception when user role is not found")
    void register_RoleNotFound_ThrowsRegistrationException() {
        // given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                "new@example.com",
                "rawPassword",
                "rawPassword",
                "John",
                "Doe",
                Gender.MALE,
                25,
                180,
                80.0,
                75.0,
                1.55,
                Goal.WEIGHT_LOSS);

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> authenticationService.register(requestDto))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("Role: " + RoleName.USER + " not found");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Should return null name when firstName and lastName are not set")
    void register_NoNamesProvided_ReturnsNullName() {
        // given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                "new@example.com",
                "rawPassword",
                "rawPassword",
                null,
                null,
                Gender.MALE,
                25,
                180,
                80.0,
                75.0,
                1.55,
                Goal.WEIGHT_LOSS);

        User userEntity = new User();
        Role userRole = new Role();
        userRole.setName(RoleName.USER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("new@example.com");
        savedUser.setFirstName(null);
        savedUser.setLastName(null);

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userMapper.toEntity(requestDto)).thenReturn(userEntity);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(userEntity)).thenReturn(savedUser);
        when(jwtUtil.generateToken("new@example.com")).thenReturn("generated-jwt-token");

        // when
        UserLoginResponseDto result = authenticationService.register(requestDto);

        // then
        assertThat(result.user().name()).isNull();
    }

    @Test
    @DisplayName("Should return only firstName when lastName is not set")
    void register_OnlyFirstNameProvided_ReturnsFirstNameWithoutTrailingSpace() {
        // given
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                "new@example.com",
                "rawPassword",
                "rawPassword",
                "Jan",
                null,
                Gender.MALE,
                25,
                180,
                80.0,
                75.0,
                1.55,
                Goal.WEIGHT_LOSS);

        User userEntity = new User();
        Role userRole = new Role();
        userRole.setName(RoleName.USER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("new@example.com");
        savedUser.setFirstName("Jan");
        savedUser.setLastName(null);

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userMapper.toEntity(requestDto)).thenReturn(userEntity);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(userEntity)).thenReturn(savedUser);
        when(jwtUtil.generateToken("new@example.com")).thenReturn("generated-jwt-token");

        // when
        UserLoginResponseDto result = authenticationService.register(requestDto);

        // then
        assertThat(result.user().name()).isEqualTo("Jan");
    }

    @Test
    @DisplayName("Should blacklist token when auth header is valid")
    void logout_ValidAuthHeader_BlacklistsToken() {
        // given
        String authHeader = "Bearer sample-jwt-token";
        when(jwtUtil.getRemainingExpirationTime("sample-jwt-token")).thenReturn(60000L);

        // when
        authenticationService.logout(authHeader);

        // then
        verify(jwtUtil).getRemainingExpirationTime("sample-jwt-token");
        verify(tokenBlacklistService).blacklistToken("sample-jwt-token", 60000L);
    }

    @Test
    @DisplayName("Should throw exception when auth header is null")
    void logout_NullAuthHeader_ThrowsException() {
        assertThatThrownBy(() -> authenticationService.logout(null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(jwtUtil, tokenBlacklistService);
    }

    @Test
    @DisplayName("Should throw exception when auth header does not start with Bearer")
    void logout_HeaderWithoutBearer_ThrowsException() {
        assertThatThrownBy(() -> authenticationService.logout("Basic sample-token"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(jwtUtil, tokenBlacklistService);
    }

    @Test
    @DisplayName("Should throw exception when auth header is blank")
    void logout_BlankAuthHeader_ThrowsException() {
        assertThatThrownBy(() -> authenticationService.logout(""))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(jwtUtil, tokenBlacklistService);
    }

    @Test
    @DisplayName("Should extract token correctly stripping Bearer prefix")
    void logout_ValidBearerHeader_ExtractsTokenCorrectly() {
        // given
        String authHeader = "Bearer abc.def.ghi";
        when(jwtUtil.getRemainingExpirationTime("abc.def.ghi")).thenReturn(3600000L);

        // when
        authenticationService.logout(authHeader);

        // then
        verify(tokenBlacklistService, times(1)).blacklistToken("abc.def.ghi", 3600000L);
    }
}
