package fitcubes.controller;

import fitcubes.dto.user.UserProfileDto;
import fitcubes.dto.user.UserProfileUpdateRequestDto;
import fitcubes.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User Profile",
        description = "Endpoints for completing/updating user profile during onboarding")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get user profile",
            description = "Returns the authenticated user's stats and macro targets.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Profile retrieved successfully"),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthorized or missing token")
            }
    )
    @GetMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileDto getProfile(Authentication authentication) {
        return userService.getProfile(authentication.getName());
    }

    @Operation(
            summary = "Update user profile",
            description = "Completes or updates the authenticated user's profile. "
                    + "Only non-null fields in the request body are applied.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Profile updated successfully"),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid payload or validation error"),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthorized or missing token")
            }
    )
    @PatchMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileDto updateProfile(
            Authentication authentication,
            @RequestBody @Valid UserProfileUpdateRequestDto requestDto) {
        return userService.updateProfile(authentication.getName(), requestDto);
    }
}
