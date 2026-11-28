package com.findit.auth;

import com.findit.common.ConflictException;
import com.findit.user.Role;
import com.findit.user.RoleName;
import com.findit.user.RoleRepository;
import com.findit.user.User;
import com.findit.user.UserRepository;
import com.findit.user.UserResponse;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registration, login and logout (token blacklisting). Public registration always creates a STUDENT. */
@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistRepository blacklistRepository;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService,
                       TokenBlacklistRepository blacklistRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.blacklistRepository = blacklistRepository;
    }

    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered");
        }
        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new IllegalStateException("ROLE_STUDENT is not seeded"));
        User user = new User(request.fullName().trim(), email, passwordEncoder.encode(request.password()));
        user.setRoles(new HashSet<>());
        user.getRoles().add(studentRole);
        User saved = userRepository.save(user);
        log.info("Registered new student id={}", saved.getId());
        return UserResponse.from(saved);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user vanished"));
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs(), UserResponse.from(user));
    }

    public void logout(String token) {
        TokenInfo info = jwtService.parse(token);
        if (!blacklistRepository.existsByJti(info.jti())) {
            blacklistRepository.save(new TokenBlacklist(info.jti(), info.expiresAt()));
        }
    }
}
