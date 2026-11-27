package com.findit.config;

import com.findit.category.Category;
import com.findit.category.CategoryRepository;
import com.findit.user.Role;
import com.findit.user.RoleName;
import com.findit.user.RoleRepository;
import com.findit.user.User;
import com.findit.user.UserRepository;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds roles, default categories and the ADMIN and SECURITY_STAFF accounts on startup. Account credentials come only
 * from environment variables (ADMIN_EMAIL, ADMIN_PASSWORD, SECURITY_EMAIL, SECURITY_PASSWORD); a pair that is blank is
 * skipped. Existing rows are never changed.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final List<String> DEFAULT_CATEGORIES =
            List.of("Electronics", "Books", "ID/Cards", "Clothing", "Keys", "Bags", "Accessories", "Other");

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String securityEmail;
    private final String securityPassword;

    public DataInitializer(RoleRepository roleRepository, CategoryRepository categoryRepository,
                           UserRepository userRepository, PasswordEncoder passwordEncoder,
                           @Value("${findit.seed.admin-email:}") String adminEmail,
                           @Value("${findit.seed.admin-password:}") String adminPassword,
                           @Value("${findit.seed.security-email:}") String securityEmail,
                           @Value("${findit.seed.security-password:}") String securityPassword) {
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.securityEmail = securityEmail;
        this.securityPassword = securityPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (RoleName name : RoleName.values()) {
            if (roleRepository.findByName(name).isEmpty()) {
                roleRepository.save(new Role(name));
            }
        }
        for (String name : DEFAULT_CATEGORIES) {
            if (!categoryRepository.existsByNameIgnoreCase(name)) {
                categoryRepository.save(new Category(name));
            }
        }
        seedUser("Administrator", adminEmail, adminPassword, RoleName.ROLE_ADMIN);
        seedUser("Security Staff", securityEmail, securityPassword, RoleName.ROLE_SECURITY_STAFF);
    }

    private void seedUser(String fullName, String email, String password, RoleName roleName) {
        if (email.isBlank() || password.isBlank()) {
            log.warn("No credentials configured for {}; account not seeded", roleName);
            return;
        }
        String normalized = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalized)) {
            return;
        }
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User(fullName, normalized, passwordEncoder.encode(password));
        user.setRoles(new HashSet<>(List.of(role)));
        userRepository.save(user);
        log.info("Seeded {} account {}", roleName, normalized);
    }
}
