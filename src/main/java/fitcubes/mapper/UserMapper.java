package fitcubes.mapper;

import fitcubes.dto.user.UserDto;
import fitcubes.dto.user.UserRegistrationRequestDto;
import fitcubes.model.Role;
import fitcubes.model.RoleName;
import fitcubes.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserRegistrationRequestDto requestDto);

    default Role map(RoleName roleName) {
        if (roleName == null) {
            return null;
        }
        Role role = new Role();
        role.setName(roleName);
        return role;
    }

    default RoleName map(Role role) {
        return (role == null) ? null : role.getName();
    }
}
