package fitcubes.dto.user;

import fitcubes.model.ActivityLevel;
import fitcubes.model.Gender;
import fitcubes.model.Goal;
import fitcubes.validation.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@PasswordMatch
public record UserRegistrationRequestDto(
        @Email
        @NotBlank(message = "Email cannot be null")
        String email,

        @NotBlank(message = "Password cannot be null")
        String password,

        @NotBlank(message = "Repeated password cannot be null")
        String repeatedPassword,

        @NotBlank(message = "First name cannot be null")
        String firstName,

        @NotBlank(message = "Last name cannot be null")
        String lastName,

        @NotNull(message = "Gender cannot be null")
        Gender gender,

        @Min(value = 16, message = "Age must be greater than 16")
        int age,

        @Min(value = 90, message = "Height must be greater than 90")
        @Max(value = 250, message = "Height must be less than 250")
        int height,

        @Min(value = 30, message = "Weight must be greater than 30")
        @Max(value = 300, message = "Weight must be less than 300")
        double currentWeight,

        @Min(value = 30, message = "Target weight must be greater than 30")
        @Max(value = 300, message = "Target weight must be less than 300")
        double targetWeight,

        @NotNull(message = "Activity level cannot be null")
        ActivityLevel activityLevel,

        @NotNull(message = "Goal cannot be null")
        Goal goal
) {
}
