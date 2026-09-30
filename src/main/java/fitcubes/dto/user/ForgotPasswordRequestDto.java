package fitcubes.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDto(
        @Email
        @NotBlank(message = "Email cannot be null")
        String email
) {
}
