package fitcubes.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email cannot be null") String email,

        @NotBlank(message = "Password cannot be null") String password
) {
}
