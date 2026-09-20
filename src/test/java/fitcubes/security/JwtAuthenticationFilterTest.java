package fitcubes.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fitcubes.model.user.Role;
import fitcubes.model.user.RoleName;
import fitcubes.model.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "some.jwt.token";
    private static final String EMAIL = "test@example.com";

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(
                jwtUtil, userDetailsService, tokenBlacklistService, authenticationEntryPoint);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("No Authorization header -> continues chain without authenticating")
    void doFilterInternal_noAuthHeader_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).getUsernameFromToken(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Header without Bearer prefix -> continues chain without authenticating")
    void doFilterInternal_headerWithoutBearerPrefix_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).getUsernameFromToken(any());
    }

    @Test
    @DisplayName("Blacklisted token -> delegates to entry point, does not continue chain")
    void doFilterInternal_blacklistedToken_delegatesToEntryPoint() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(TOKEN)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationEntryPoint).commence(eq(request), eq(response), any());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Expired token (JwtException) -> delegates to entry point, not 500")
    void doFilterInternal_expiredToken_delegatesToEntryPoint() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(TOKEN)).thenReturn(false);
        when(jwtUtil.getUsernameFromToken(TOKEN))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationEntryPoint).commence(eq(request), eq(response), any());
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Malformed token (JwtException) -> delegates to entry point, not 500")
    void doFilterInternal_malformedToken_delegatesToEntryPoint() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer garbage");
        when(tokenBlacklistService.isTokenBlacklisted("garbage")).thenReturn(false);
        when(jwtUtil.getUsernameFromToken("garbage"))
                .thenThrow(new MalformedJwtException("Malformed token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationEntryPoint).commence(eq(request), eq(response), any());
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Valid token -> sets authentication and continues chain")
    void doFilterInternal_validToken_setsAuthenticationAndContinues() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);
        Role userRole = new Role();
        userRole.setName(RoleName.USER);
        user.setRoles(java.util.Set.of(userRole));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(TOKEN)).thenReturn(false);
        when(jwtUtil.getUsernameFromToken(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(user);
        when(jwtUtil.isValidToken(TOKEN)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo(EMAIL);
        verify(filterChain).doFilter(request, response);
        verify(authenticationEntryPoint, never()).commence(any(), any(), any());
    }

    @Test
    @DisplayName("Token fails final validity check -> does not authenticate, still continues chain")
    void doFilterInternal_tokenNotValid_doesNotAuthenticateButContinues() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(TOKEN)).thenReturn(false);
        when(jwtUtil.getUsernameFromToken(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(user);
        when(jwtUtil.isValidToken(TOKEN)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
