package fitcubes.validation;

import static org.assertj.core.api.Assertions.assertThat;

import fitcubes.dto.user.UserRegistrationRequestDto;
import fitcubes.model.ActivityLevel;
import fitcubes.model.Gender;
import fitcubes.model.Goal;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserRegistrationRequestDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Should have no violations when passwords match")
    void validate_matchingPasswords_noViolation() {
        UserRegistrationRequestDto dto = createDto("password123", "password123");

        Set<ConstraintViolation<UserRegistrationRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should have violation on repeatedPassword when passwords differ")
    void validate_differentPasswords_hasViolation() {
        UserRegistrationRequestDto dto = createDto("password123", "differentPassword");

        Set<ConstraintViolation<UserRegistrationRequestDto>> violations = validator.validate(dto);

        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("repeatedPassword"));
    }

    private UserRegistrationRequestDto createDto(String password, String repeatedPassword) {
        return new UserRegistrationRequestDto(
                "test@example.com", password, repeatedPassword,
                "John", "Doe", Gender.MALE, 25, 180, 80.0, 75.0,
                ActivityLevel.MODERATELY_ACTIVE, Goal.WEIGHT_LOSS
        );
    }
}