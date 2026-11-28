package com.findit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.findit.TestData;
import com.findit.user.RoleName;
import com.findit.user.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-that-is-longer-than-32-bytes!!";

    private final User user = TestData.user(5L, "Ann", RoleName.ROLE_STUDENT);

    @Test
    void generatedToken_canBeParsed() {
        JwtService service = new JwtService(SECRET, 60_000);

        TokenInfo info = service.parse(service.generateToken(user));

        assertEquals(user.getEmail(), info.subject());
        assertNotNull(info.jti());
        assertNotNull(info.expiresAt());
    }

    @Test
    void tamperedToken_isRejected() {
        JwtService service = new JwtService(SECRET, 60_000);
        String token = service.generateToken(user);

        assertThrows(JwtException.class, () -> service.parse(token + "x"));
    }

    @Test
    void expiredToken_isRejected() {
        JwtService service = new JwtService(SECRET, -1_000);
        String token = service.generateToken(user);

        assertThrows(JwtException.class, () -> service.parse(token));
    }
}
