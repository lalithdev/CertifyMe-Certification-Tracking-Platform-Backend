package com.certifyme.app.repository;

import com.certifyme.app.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByEmailAndOtp(String email, String otp);
    Optional<PasswordResetToken> findTopByEmailOrderByCreatedAtDesc(String email);
}
