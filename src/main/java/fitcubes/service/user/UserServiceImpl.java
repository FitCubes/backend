package fitcubes.service.user;

import fitcubes.dto.user.UserDto;
import fitcubes.dto.user.UserProfileUpdateRequestDto;
import fitcubes.exception.UserNotFoundException;
import fitcubes.mapper.UserMapper;
import fitcubes.model.user.User;
import fitcubes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto updateProfile(String email, UserProfileUpdateRequestDto requestDto) {
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

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }
}
