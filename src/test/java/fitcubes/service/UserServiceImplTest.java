package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fitcubes.dto.user.UserProfileDto;
import fitcubes.dto.user.UserProfileUpdateRequestDto;
import fitcubes.exception.UserNotFoundException;
import fitcubes.model.user.ActivityLevel;
import fitcubes.model.user.DietStrategy;
import fitcubes.model.user.Gender;
import fitcubes.model.user.Goal;
import fitcubes.model.user.User;
import fitcubes.repository.UserRepository;
import fitcubes.service.dashboard.CalorieCalculationService;
import fitcubes.service.user.impl.UserServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CalorieCalculationService calorieCalculationService;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String EMAIL = "test@example.com";

    private User buildCompleteUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setGender(Gender.MALE);
        user.setAge(30);
        user.setHeight(180);
        user.setCurrentWeight(85.5);
        user.setTargetWeight(78.0);
        user.setActivityLevel(ActivityLevel.MODERATELY_ACTIVE);
        user.setGoal(Goal.MAINTENANCE);
        user.setDietStrategy(DietStrategy.BALANCED);
        user.setProteinTargetGrams(154);
        user.setCarbsTargetGrams(321);
        user.setFatsTargetGrams(107);
        return user;
    }

    // ---------- getProfile ----------

    @Test
    void getProfile_completeUser_returnsFullProfileWithCalories() {
        User user = buildCompleteUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(calorieCalculationService.calculateTargetCalories(user))
                .thenReturn(BigDecimal.valueOf(2860));

        UserProfileDto result = userService.getProfile(EMAIL);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.name()).isEqualTo("John Doe");
        assertThat(result.weightKg()).isEqualTo(85.5);
        assertThat(result.heightCm()).isEqualTo(180.0);
        assertThat(result.dietStrategy()).isEqualTo(DietStrategy.BALANCED);
        assertThat(result.macroTargets().calories()).isEqualTo(2860);
        assertThat(result.macroTargets().proteinGrams()).isEqualTo(154);
        assertThat(result.macroTargets().carbsGrams()).isEqualTo(321);
        assertThat(result.macroTargets().fatsGrams()).isEqualTo(107);
    }

    @Test
    void getProfile_incompleteUser_returnsNullCalories() {
        User user = new User();
        user.setId(2L);
        user.setEmail(EMAIL);
        // brak wagi/wzrostu/wieku itd. -> profil niekompletny

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserProfileDto result = userService.getProfile(EMAIL);

        assertThat(result.macroTargets().calories()).isNull();
        assertThat(result.macroTargets().proteinGrams()).isNull();
    }

    @Test
    void getProfile_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(EMAIL))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getProfile_noNames_returnsNullName() {
        User user = new User();
        user.setId(3L);
        user.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserProfileDto result = userService.getProfile(EMAIL);

        assertThat(result.name()).isNull();
    }

    // ---------- updateProfile ----------

    @Test
    void updateProfile_updatesOnlyProvidedFields() {
        User existingUser = buildCompleteUser();
        UserProfileUpdateRequestDto requestDto = new UserProfileUpdateRequestDto(
                null, null, null, null, null,
                90.0, null, null, null,
                DietStrategy.KETO, 171, 45, 222
        );

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(calorieCalculationService.calculateTargetCalories(existingUser))
                .thenReturn(BigDecimal.valueOf(2900));

        UserProfileDto result = userService.updateProfile(EMAIL, requestDto);

        assertThat(existingUser.getCurrentWeight()).isEqualTo(90.0);
        assertThat(existingUser.getFirstName()).isEqualTo("John"); // niezmienione
        assertThat(existingUser.getDietStrategy()).isEqualTo(DietStrategy.KETO);
        assertThat(existingUser.getProteinTargetGrams()).isEqualTo(171);
        assertThat(existingUser.getCarbsTargetGrams()).isEqualTo(45);
        assertThat(existingUser.getFatsTargetGrams()).isEqualTo(222);

        assertThat(result.macroTargets().proteinGrams()).isEqualTo(171);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateProfile_allNullFields_doesNotChangeAnything() {
        User existingUser = buildCompleteUser();
        UserProfileUpdateRequestDto emptyRequest = new UserProfileUpdateRequestDto(
                null, null, null, null, null, null, null, null, null,
                null, null, null, null
        );

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(calorieCalculationService.calculateTargetCalories(existingUser))
                .thenReturn(BigDecimal.valueOf(2860));

        userService.updateProfile(EMAIL, emptyRequest);

        assertThat(existingUser.getCurrentWeight()).isEqualTo(85.5);
        assertThat(existingUser.getDietStrategy()).isEqualTo(DietStrategy.BALANCED);
    }

    @Test
    void updateProfile_userNotFound_throwsUserNotFoundException() {
        UserProfileUpdateRequestDto requestDto = new UserProfileUpdateRequestDto(
                null, null, null, null, null, null, null, null, null,
                null, null, null, null
        );

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(EMAIL, requestDto))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }
}
