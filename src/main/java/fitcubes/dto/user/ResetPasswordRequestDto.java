package fitcubes.dto.user;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDto(
        @NotBlank(message = "Token cannot be null")
        String token,

        @NotBlank(message = "New password cannot be null")
        String newPassword
) {
}
