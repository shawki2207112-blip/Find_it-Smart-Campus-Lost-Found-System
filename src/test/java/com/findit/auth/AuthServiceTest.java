package com.findit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.findit.TestData;
import com.findit.common.ConflictException;
import com.findit.user.Role;
import com.findit.user.RoleName;
import com.findit.user.RoleRepository;
import com.findit.user.User;
import com.findit.user.UserRepository;
import com.findit.user.UserResponse;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenBlacklistRepository blacklistRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userRepository.existsByEmail("ann@test.local")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> authService.register(new RegisterRequest("Ann", "ann@test.local", "password1")));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_encodesPassword_andAssignsStudentRole() {
        when(userRepository.existsByEmail("ann@test.local")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_STUDENT)).thenReturn(Optional.of(new Role(RoleName.ROLE_STUDENT)));
        when(passwordEncoder.encode("password1")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService.register(new RegisterRequest("Ann", " Ann@Test.local ", "password1"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("ENCODED", captor.getValue().getPassword());
        assertEquals("ann@test.local", captor.getValue().getEmail());
        assertTrue(response.roles().contains("ROLE_STUDENT"));
        assertEquals(1, response.roles().size());
    }

    @Test
    void login_badCredentials_throwsAndIssuesNoToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("ann@test.local", "wrong")));

        verifyNoInteractions(jwtService);
    }

    @Test
    void login_validCredentials_returnsToken() {
        User user = TestData.user(1L, "Ann", RoleName.ROLE_STUDENT);
        when(userRepository.findByEmail("ann@test.local")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(1000L);

        AuthResponse response = authService.login(new LoginRequest("Ann@test.local", "password1"));

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void logout_blacklistsTokenJtiWithExpiry() {
        Instant expiry = Instant.now().plusSeconds(600);
        when(jwtService.parse("jwt-token")).thenReturn(new TokenInfo("jti-1", "ann@test.local", expiry));
        when(blacklistRepository.existsByJti("jti-1")).thenReturn(false);

        authService.logout("jwt-token");

        ArgumentCaptor<TokenBlacklist> captor = ArgumentCaptor.forClass(TokenBlacklist.class);
        verify(blacklistRepository).save(captor.capture());
        assertEquals("jti-1", captor.getValue().getJti());
        assertEquals(expiry, captor.getValue().getExpiresAt());
    }

    @Test
    void logout_tokenAlreadyBlacklisted_isNotSavedTwice() {
        when(jwtService.parse("jwt-token")).thenReturn(new TokenInfo("jti-1", "ann@test.local", Instant.now()));
        when(blacklistRepository.existsByJti("jti-1")).thenReturn(true);

        authService.logout("jwt-token");

        verify(blacklistRepository, never()).save(any(TokenBlacklist.class));
    }
}
