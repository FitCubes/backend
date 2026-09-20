package fitcubes.service.user.impl;

import fitcubes.dto.user.MacroTargetsDto;
import fitcubes.dto.user.UserProfileDto;
import fitcubes.dto.user.UserProfileUpdateRequestDto;
import fitcubes.exception.UserNotFoundException;
import fitcubes.model.user.User;
import fitcubes.repository.UserRepository;
import fitcubes.service.dashboard.CalorieCalculationService;
import fitcubes.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CalorieCalculationService calorieCalculationService;

    @Override
    public UserProfileDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        return toProfileDto(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#email")
    public UserProfileDto updateProfile(String email, UserProfileUpdateRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        if (requestDto.firstName() != null) {
            user.setFirstName(requestDto.firstName());
        }
        if (requestDto.lastName() != null) {
            user.setLastName(requestDto.lastName());
        }
        if (requestDto.gender() != null) {
            user.setGender(requestDto.gender());
        }
        if (requestDto.age() != null) {
            user.setAge(requestDto.age());
        }
        if (requestDto.height() != null) {
            user.setHeight(requestDto.height());
        }
        if (requestDto.currentWeight() != null) {
            user.setCurrentWeight(requestDto.currentWeight());
        }
        if (requestDto.targetWeight() != null) {
            user.setTargetWeight(requestDto.targetWeight());
        }
        if (requestDto.activityLevel() != null) {
            user.setActivityLevel(requestDto.activityLevel());
        }
        if (requestDto.goal() != null) {
            user.setGoal(requestDto.goal());
        }
        if (requestDto.dietStrategy() != null) {
            user.setDietStrategy(requestDto.dietStrategy());
        }
        if (requestDto.proteinTargetGrams() != null) {
            user.setProteinTargetGrams(requestDto.proteinTargetGrams());
        }
        if (requestDto.carbsTargetGrams() != null) {
            user.setCarbsTargetGrams(requestDto.carbsTargetGrams());
        }
        if (requestDto.fatsTargetGrams() != null) {
            user.setFatsTargetGrams(requestDto.fatsTargetGrams());
        }

        User savedUser = userRepository.save(user);
        return toProfileDto(savedUser);
    }

    private UserProfileDto toProfileDto(User user) {
        Integer calories = hasBasics(user)
                ? calorieCalculationService.calculateTargetCalories(user).intValue()
                : null;

        MacroTargetsDto macroTargets = new MacroTargetsDto(
                calories,
                user.getProteinTargetGrams(),
                user.getCarbsTargetGrams(),
                user.getFatsTargetGrams()
        );

        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                buildName(user),
                user.getCurrentWeight(),
                user.getHeight() != null ? user.getHeight().doubleValue() : null,
                user.getActivityLevel(),
                user.getDietStrategy(),
                macroTargets
        );
    }

    private boolean hasBasics(User user) {
        return user.getCurrentWeight() != null && user.getHeight() != null
                && user.getAge() != null && user.getGender() != null
                && user.getActivityLevel() != null && user.getGoal() != null;
    }

    private String buildName(User user) {
        if (user.getFirstName() == null && user.getLastName() == null) {
            return null;
        }
        return (user.getFirstName() != null ? user.getFirstName() : "")
                + (user.getLastName() != null ? " " + user.getLastName() : "");
    }
}
