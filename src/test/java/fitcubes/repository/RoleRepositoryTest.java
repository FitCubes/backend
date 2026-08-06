package fitcubes.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import fitcubes.config.TestcontainersConfiguration;
import fitcubes.model.Role;
import fitcubes.model.RoleName;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Should successfully retrieve a security role by its unique RoleName enum")
    void findByNames_validName_returnsRole() {
        // GIVEN
        RoleName roleName = RoleName.USER;

        // WHEN
        Optional<Role> result = roleRepository.findByName(roleName);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(roleName);
    }
}
