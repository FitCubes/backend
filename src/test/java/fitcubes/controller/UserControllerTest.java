package fitcubes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fitcubes.dto.user.MacroTargetsDto;
import fitcubes.dto.user.UserProfileDto;
import fitcubes.dto.user.UserProfileUpdateRequestDto;
import fitcubes.model.user.DietStrategy;
import fitcubes.security.CustomAuthenticationEntryPoint;
import fitcubes.security.JwtUtil;
import fitcubes.security.TokenBlacklistService;
import fitcubes.service.user.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String EMAIL = "test@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    private UserProfileDto profileDto;

    @BeforeEach
    void setUp() {
        MacroTargetsDto macros = new MacroTargetsDto(2860, 154, 321, 107);
        profileDto = new UserProfileDto(
                1L, EMAIL, "John Doe", 85.5, 180.0, "1.55",
                DietStrategy.BALANCED, macros
        );
    }

    private RequestPostProcessor asUser() {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        EMAIL, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    @Test
    void getProfile_returnsOk() throws Exception {
        when(userService.getProfile(EMAIL)).thenReturn(profileDto);

        mockMvc.perform(get("/api/v1/users/profile").with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.macroTargets.proteinGrams").value(154));
    }

    @Test
    void getProfile_unauthenticated_returns401Or403() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateProfile_returnsOk() throws Exception {
        UserProfileUpdateRequestDto requestDto = new UserProfileUpdateRequestDto(
                null, null, null, null, null,
                90.0, null, null, null,
                DietStrategy.KETO, 171, 45, 222
        );

        when(userService.updateProfile(eq(EMAIL), any())).thenReturn(profileDto);

        mockMvc.perform(patch("/api/v1/users/profile")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void updateProfile_invalidAge_returnsBadRequest() throws Exception {
        UserProfileUpdateRequestDto invalid = new UserProfileUpdateRequestDto(
                null, null, null, 10, null,
                null, null, null, null,
                null, null, null, null
        );

        mockMvc.perform(patch("/api/v1/users/profile")
                        .with(csrf())
                        .with(asUser())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
