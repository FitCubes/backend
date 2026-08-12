package fitcubes.dto.user;

import fitcubes.model.user.RoleName;
import java.util.Set;

public record UserDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        Set<RoleName> roles
) {
}
