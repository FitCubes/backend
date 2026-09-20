package fitcubes.dto.user;

import fitcubes.model.user.DietStrategy;
import fitcubes.model.user.Gender;
import fitcubes.model.user.Goal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

        @DecimalMin(value = "1.2", message = "Activity level must be at least 1.2")
        @DecimalMax(value = "1.9", message = "Activity level must be at most 1.9")
        Double activityLevel,

        Goal goal,

        DietStrategy dietStrategy,

        @Min(value = 0, message = "Protein target must not be negative")
        Integer proteinTargetGrams,

        @Min(value = 0, message = "Carbs target must not be negative")
        Integer carbsTargetGrams,

        @Min(value = 0, message = "Fats target must not be negative")
        Integer fatsTargetGrams
) {
}
