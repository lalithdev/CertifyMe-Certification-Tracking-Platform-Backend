package com.certifyme.app.controller;

import com.certifyme.app.dto.*;
import com.certifyme.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<AuthResponseDTO> resendOtp(@Valid @RequestBody ResendOtpRequestDTO request) {
        return ResponseEntity.ok(authService.resendOtp(request.getEmail()));
    }

    /**
     * POST /api/auth/forgot-password
     * Initiates the password reset flow by sending an OTP to the registered email.
     * Works for both STUDENT and ADMIN roles.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        log.info("[AuthController] POST /api/auth/forgot-password → email={}", request.getEmail());
        AuthResponseDTO response = authService.forgotPassword(request.getEmail());
        log.info("[AuthController] forgot-password completed successfully for: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/verify-otp
     * Verifies the OTP and returns a JWT session.
     * Kept for backward compatibility with existing frontend routes.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDTO> verifyOtp(@Valid @RequestBody VerifyOtpRequestDTO request) {
        log.info("[AuthController] POST /api/auth/verify-otp → email={}", request.getEmail());
        return ResponseEntity.ok(authService.verifyResetOtp(request.getEmail(), request.getOtp()));
    }

    /**
     * POST /api/auth/verify-reset-otp
     * Alias for verify-otp — dedicated endpoint for the password reset flow.
     */
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<AuthResponseDTO> verifyResetOtp(@Valid @RequestBody VerifyOtpRequestDTO request) {
        log.info("[AuthController] POST /api/auth/verify-reset-otp → email={}", request.getEmail());
        return ResponseEntity.ok(authService.verifyResetOtp(request.getEmail(), request.getOtp()));
    }

    /**
     * POST /api/auth/reset-password
     * Resets the user's password after OTP verification.
     * Requires: email, otp, newPassword.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        log.info("[AuthController] POST /api/auth/reset-password → email={}", request.getEmail());
        return ResponseEntity.ok(authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword()));
    }

    /**
     * POST /api/auth/change-password
     * Changes the password for an authenticated user.
     * Requires: JWT token, currentPassword, newPassword.
     */
    @PostMapping("/change-password")
    public ResponseEntity<AuthResponseDTO> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        log.info("[AuthController] POST /api/auth/change-password → user={}", userDetails.getUsername());
        return ResponseEntity.ok(authService.changePassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword()));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend is running correctly!");
    }
}
