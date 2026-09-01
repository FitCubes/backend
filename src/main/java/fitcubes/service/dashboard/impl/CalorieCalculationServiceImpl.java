package fitcubes.service.dashboard.impl;

import fitcubes.model.user.Gender;
import fitcubes.model.user.User;
import fitcubes.service.dashboard.CalorieCalculationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class CalorieCalculationServiceImpl implements CalorieCalculationService {

    private static final int SCALE = 2;
    private static final BigDecimal LOSS_MODIFIER = BigDecimal.valueOf(0.85);
    private static final BigDecimal GAIN_MODIFIER = BigDecimal.valueOf(1.15);
    private static final BigDecimal SAFE_MINIMUM_FEMALE = BigDecimal.valueOf(1200);
    private static final BigDecimal SAFE_MINIMUM_MALE = BigDecimal.valueOf(1500);

    @Override
    public BigDecimal calculateBmr(User user) {
        double weight = user.getCurrentWeight();
        double height = user.getHeight();
        double age = user.getAge();

        double bmr = switch (user.getGender()) {
            case MALE -> 10 * weight + 6.25 * height - 5 * age + 5;
            case FEMALE -> 10 * weight + 6.25 * height - 5 * age - 161;
        };

        return BigDecimal.valueOf(bmr).setScale(SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTdee(User user) {
        BigDecimal bmr = calculateBmr(user);
        BigDecimal factor = BigDecimal.valueOf(user.getActivityLevel().getFactor());

        return bmr.multiply(factor).setScale(SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTargetCalories(User user) {
        BigDecimal tdee = calculateTdee(user);

        BigDecimal modifier = switch (user.getGoal()) {
            case WEIGHT_LOSS -> LOSS_MODIFIER;
            case MAINTENANCE -> BigDecimal.ONE;
            case MUSCLE_GAIN -> GAIN_MODIFIER;
        };

        BigDecimal target = tdee.multiply(modifier).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal safeMinimum = user.getGender() == Gender.FEMALE
                ? SAFE_MINIMUM_FEMALE : SAFE_MINIMUM_MALE;

        return target.max(safeMinimum);
    }
}
