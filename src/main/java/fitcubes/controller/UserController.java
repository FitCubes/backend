package fitcubes.controller;

import fitcubes.dto.user.UserDto;
import fitcubes.dto.user.UserProfileUpdateRequestDto;
import fitcubes.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
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
    @PutMapping("/profile")
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateProfile(Authentication authentication,
                                 @RequestBody @Valid UserProfileUpdateRequestDto requestDto) {
        return userService.updateProfile(authentication.getName(), requestDto);
    }
}
