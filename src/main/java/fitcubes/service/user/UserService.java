package fitcubes.service.user;

import fitcubes.dto.user.UserProfileDto;
import fitcubes.dto.user.UserProfileUpdateRequestDto;

public interface UserService {

    UserProfileDto getProfile(String email);

    UserProfileDto updateProfile(String email, UserProfileUpdateRequestDto requestDto);
}
