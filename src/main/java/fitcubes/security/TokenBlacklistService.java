package fitcubes.security;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklisted_token:";
    private final StringRedisTemplate redisTemplate;

    public void blacklistToken(String token, long remainingTimeToExpirationMs) {
        if (remainingTimeToExpirationMs > 0) {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "logout",
                    Duration.ofMillis(remainingTimeToExpirationMs));
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
