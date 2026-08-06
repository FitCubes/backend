package fitcubes.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtConfig {
    @NotBlank(message = "JWT secret must be configured with JWT_SECRET or jwt.secret")
    private String secret;

    @Min(value = 1, message = "JWT expiration must be greater than 0")
    private long expiration;
}
