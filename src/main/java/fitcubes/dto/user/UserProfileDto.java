package fitcubes.dto.user;

import fitcubes.model.user.DietStrategy;

public record UserProfileDto(
        Long id,
        String email,
        String name,
        Double weightKg,
        Double heightCm,
        Double activityFactor,
        DietStrategy dietStrategy,
        MacroTargetsDto macroTargets
) {
}
