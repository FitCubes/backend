package fitcubes.dto.user;

import fitcubes.model.user.ActivityLevel;
import fitcubes.model.user.Gender;
import fitcubes.model.user.Goal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UserProfileUpdateRequestDto(
        String firstName,

        String lastName,

        Gender gender,

        @Min(value = 16, message = "Age must be greater than 16")
        Integer age,

        @Min(value = 90, message = "Height must be greater than 90")
        @Max(value = 250, message = "Height must be less than 250")
        Integer height,

        @Min(value = 30, message = "Weight must be greater than 30")
        @Max(value = 300, message = "Weight must be less than 300")
        Double currentWeight,

        @Min(value = 30, message = "Target weight must be greater than 30")
        @Max(value = 300, message = "Target weight must be less than 300")
        Double targetWeight,

        ActivityLevel activityLevel,

        Goal goal
) {
}
