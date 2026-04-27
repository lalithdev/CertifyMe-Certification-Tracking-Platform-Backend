package com.certifyme.app.service;

import com.certifyme.app.model.User;
import com.certifyme.app.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataMigrationComponent implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataMigrationComponent.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataMigrationComponent(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Checks if a password string is a VALID BCrypt hash.
     * A real BCrypt hash: starts with $2a$/$2b$/$2y$, is exactly 60 characters.
     * This prevents fabricated hashes (correct prefix but wrong content) from
     * being skipped during migration.
     */
    private boolean isValidBCryptHash(String pwd) {
        if (pwd == null || pwd.length() != 60) return false;
        return pwd.startsWith("$2a$") || pwd.startsWith("$2b$") || pwd.startsWith("$2y$");
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("[DataMigration] Checking for non-BCrypt passwords to migrate...");
        List<User> users = userRepository.findAll();
        int migratedCount = 0;
        for (User user : users) {
            String pwd = user.getPassword();
            if (!isValidBCryptHash(pwd)) {
                log.warn("[DataMigration] Migrating password for user id={} email={} (was not a valid BCrypt hash)",
                        user.getId(), user.getEmail());
                user.setPassword(passwordEncoder.encode(pwd));
                userRepository.save(user);
                migratedCount++;
            }
        }
        if (migratedCount > 0) {
            log.info("[DataMigration] Migrated {} password(s) to BCrypt.", migratedCount);
        } else {
            log.info("[DataMigration] All passwords are already valid BCrypt hashes. No migration needed.");
        }

        // Add a default admin if none exists so users can test OTP admin flow safely
        if (!userRepository.existsByEmail("verify.certifyme@gmail.com")) {
            User adminUser = User.builder()
                .firstName("Default")
                .lastName("Admin")
                .email("verify.certifyme@gmail.com")
                .password(passwordEncoder.encode("admin123"))
                .role(com.certifyme.app.model.Role.ADMIN)
                .age(30)
                .gender("Prefer not to say")
                .country("United States")
                .otpAttempts(0)
                .build();

            userRepository.save(adminUser);

            log.info(">>> Created default Admin account: verify.certifyme@gmail.com / password: admin123 <<<");
        }
        
    }
}
