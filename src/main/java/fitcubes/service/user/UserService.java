package fitcubes.service.user;

import fitcubes.dto.user.UserDto;
import fitcubes.dto.user.UserProfileUpdateRequestDto;

public interface UserService {

    UserDto updateProfile(String email, UserProfileUpdateRequestDto requestDto);
}
