package fitcubes.service;

import static org.assertj.core.api.Assertions.assertThat;

import fitcubes.model.user.ActivityLevel;
import fitcubes.model.user.Gender;
import fitcubes.model.user.Goal;
import fitcubes.model.user.User;
import fitcubes.service.dashboard.impl.CalorieCalculationServiceImpl;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CalorieCalculationServiceImplTest {

    private final CalorieCalculationServiceImpl service = new CalorieCalculationServiceImpl();

    private User buildUser(Gender gender, double weight, int height, int age,
                           ActivityLevel activityLevel, Goal goal) {
        User user = new User();
        user.setGender(gender);
        user.setCurrentWeight(weight);
        user.setHeight(height);
        user.setAge(age);
        user.setActivityLevel(activityLevel);
        user.setGoal(goal);
        return user;
    }

    @Test
    void calculateBmr_male_usesCorrectFormula() {
        User user = buildUser(Gender.MALE, 80, 180, 30,
                ActivityLevel.SEDENTARY, Goal.MAINTENANCE);

        BigDecimal result = service.calculateBmr(user);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1780.00));
    }

    @Test
    void calculateBmr_female_usesCorrectFormula() {
        User user = buildUser(Gender.FEMALE, 65, 165, 25,
                ActivityLevel.SEDENTARY, Goal.MAINTENANCE);

        BigDecimal result = service.calculateBmr(user);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1395.25));
    }

    @Test
    void calculateTdee_appliesActivityFactor() {
        User user = buildUser(Gender.MALE, 80, 180, 30,
                ActivityLevel.MODERATELY_ACTIVE, Goal.MAINTENANCE);

        BigDecimal result = service.calculateTdee(user);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(2759.00));
    }

    @Test
    void calculateTdee_sedentaryVsVeryActive_producesDifferentResults() {
        User sedentary = buildUser(Gender.MALE, 80, 180, 30,
                ActivityLevel.SEDENTARY, Goal.MAINTENANCE);
        User veryActive = buildUser(Gender.MALE, 80, 180, 30,
                ActivityLevel.VERY_ACTIVE, Goal.MAINTENANCE);

        BigDecimal resultSedentary = service.calculateTdee(sedentary);
        BigDecimal resultVeryActive = service.calculateTdee(veryActive);

        assertThat(resultSedentary).isLessThan(resultVeryActive);
    }

    @Test
    void calculateTargetCalories_maintenance_equalsTdee() {
        User user = buildUser(Gender.MALE, 80, 180, 30,
                ActivityLevel.SEDENTARY, Goal.MAINTENANCE);

        BigDecimal tdee = service.calculateTdee(user);
        BigDecimal target = service.calculateTargetCalories(user);

        assertThat(target).isEqualByComparingTo(tdee);
    }

    @Test
    void calculateTargetCalories_weightLoss_appliesDeficit() {
        User user = buildUser(Gender.MALE, 80, 180, 30,
                ActivityLevel.MODERATELY_ACTIVE, Goal.WEIGHT_LOSS);

        BigDecimal target = service.calculateTargetCalories(user);

        assertThat(target).isEqualByComparingTo(BigDecimal.valueOf(2345.15));
    }

    @Test
    void calculateTargetCalories_muscleGain_appliesSurplus() {
        User user = buildUser(Gender.MALE, 80, 180, 30,
                ActivityLevel.MODERATELY_ACTIVE, Goal.MUSCLE_GAIN);

        BigDecimal target = service.calculateTargetCalories(user);

        assertThat(target).isEqualByComparingTo(BigDecimal.valueOf(3172.85));
    }

    @Test
    void calculateTargetCalories_veryLowDeficit_clampedToSafeMinimumFemale() {
        User user = buildUser(Gender.FEMALE, 45, 150, 65,
                ActivityLevel.SEDENTARY, Goal.WEIGHT_LOSS);

        BigDecimal target = service.calculateTargetCalories(user);

        assertThat(target).isEqualByComparingTo(BigDecimal.valueOf(1200));
    }

    @Test
    void calculateTargetCalories_veryLowDeficit_clampedToSafeMinimumMale() {
        User user = buildUser(Gender.MALE, 50, 155, 70,
                ActivityLevel.SEDENTARY, Goal.WEIGHT_LOSS);

        BigDecimal target = service.calculateTargetCalories(user);

        assertThat(target).isEqualByComparingTo(BigDecimal.valueOf(1500));
    }
}
