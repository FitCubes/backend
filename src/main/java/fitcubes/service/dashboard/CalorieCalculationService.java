package fitcubes.service.dashboard;

import fitcubes.model.user.User;
import java.math.BigDecimal;

public interface CalorieCalculationService {

    BigDecimal calculateBmr(User user);

    BigDecimal calculateTdee(User user);

    BigDecimal calculateTargetCalories(User user);
}
