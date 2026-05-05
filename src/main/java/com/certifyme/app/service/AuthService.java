package com.certifyme.app.service;

import com.certifyme.app.dto.AuthResponseDTO;
import com.certifyme.app.dto.LoginRequestDTO;
import com.certifyme.app.dto.RegisterRequestDTO;
import com.certifyme.app.dto.UserResponseDTO;
import com.certifyme.app.exception.DuplicateResourceException;
import com.certifyme.app.exception.UnauthorizedException;
import com.certifyme.app.mapper.UserMapper;
import com.certifyme.app.model.PasswordResetToken;
import com.certifyme.app.model.Role;
import com.certifyme.app.model.User;
import com.certifyme.app.repository.PasswordResetTokenRepository;
import com.certifyme.app.repository.UserRepository;
import com.certifyme.app.security.JwtService;
import com.certifyme.app.util.OtpUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthService(UserRepository userRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       EmailService emailService,
                       PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        UserResponseDTO userDTO = userMapper.toResponseDTO(user);

        return AuthResponseDTO.ofSuccess(jwtToken, userDTO);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        // 1. First authenticate with credentials (always required)
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // 2. If user is ADMIN, check OTP flow
        if (user.getRole() == Role.ADMIN) {
            // Check Lockout
            if (user.getOtpLockedUntil() != null && user.getOtpLockedUntil().isAfter(LocalDateTime.now())) {
                long minutesLeft = java.time.Duration.between(LocalDateTime.now(), user.getOtpLockedUntil()).toMinutes() + 1;
                throw new UnauthorizedException("Too many attempts. Locked out. Try again in " + minutesLeft + " minutes.");
            }

            String submittedOtp = request.getOtp();

            // Case A: No OTP provided - generate and send
            if (submittedOtp == null || submittedOtp.isBlank()) {
                String otp = OtpUtils.generateOTP();
                user.setVerificationCode(OtpUtils.hashOTP(otp)); // SECURE HASH
                user.setOtpCreatedAt(LocalDateTime.now());
                user.setOtpAttempts(0); // RESET ON NEW GEN
                userRepository.save(user);

                // Dispatch OTP email immediately using the async worker
                System.out.println("OTP requested for email: " + user.getEmail());
                emailService.sendOtpEmail(user.getEmail(), otp);

                // Return timers for initial demand
                return AuthResponseDTO.builder()
                        .otpRequired(true)
                        .remainingValiditySeconds(120L)
                        .resendCooldownSeconds(30L)
                        .build();
            }

            // Case B: OTP provided - validate
            String hashedInput = OtpUtils.hashOTP(submittedOtp);

            // Check Expiry (2 Minutes)
            if (user.getOtpCreatedAt() == null ||
                user.getOtpCreatedAt().isBefore(LocalDateTime.now().minusMinutes(2))) {
                throw new UnauthorizedException("OTP expired");
            }

            if (!hashedInput.equals(user.getVerificationCode())) {
                user.setOtpAttempts(user.getOtpAttempts() + 1);
                if (user.getOtpAttempts() >= 3) {
                    user.setOtpLockedUntil(LocalDateTime.now().plusMinutes(5));
                    userRepository.save(user);
                    throw new UnauthorizedException("Too many attempts, try again later");
                }
                userRepository.save(user);
                throw new UnauthorizedException("Invalid OTP");
            }

            // Valid! Clear the OTP for security and fall through to generate JWT
            user.setVerificationCode(null);
            user.setOtpCreatedAt(null);
            user.setOtpAttempts(0);
            user.setOtpLockedUntil(null);
            userRepository.save(user);
        }

        // 3. Generate token for validated user (Student or Admin with valid OTP)
        String jwtToken = jwtService.generateToken(user);
        UserResponseDTO userDTO = userMapper.toResponseDTO(user);

        return AuthResponseDTO.ofSuccess(jwtToken, userDTO);
    }

    public AuthResponseDTO resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Action not permitted");
        }

        // Check Lockout
        if (user.getOtpLockedUntil() != null && user.getOtpLockedUntil().isAfter(LocalDateTime.now())) {
            throw new UnauthorizedException("Account locked. Please wait.");
        }

        // Check Cooldown (30 seconds)
        if (user.getOtpCreatedAt() != null) {
            long secondsSinceLast = java.time.Duration.between(user.getOtpCreatedAt(), LocalDateTime.now()).getSeconds();
            if (secondsSinceLast < 30) {
                throw new UnauthorizedException("Please wait " + (30 - secondsSinceLast) + "s before resending.");
            }
        }

        // Generate NEW OTP
        String otp = OtpUtils.generateOTP();
        user.setVerificationCode(OtpUtils.hashOTP(otp)); // SECURE HASH
        user.setOtpCreatedAt(LocalDateTime.now());
        user.setOtpAttempts(0); // Reset attempts on resend
        userRepository.save(user);

        // Send Email
        emailService.sendOtpEmail(user.getEmail(), otp);

        return AuthResponseDTO.builder()
                .otpRequired(true)
                .remainingValiditySeconds(120L)
                .resendCooldownSeconds(30L)
                .build();
    }

    public AuthResponseDTO forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User with this email does not exist"));

        String otp = OtpUtils.generateOTP();
        PasswordResetToken token = PasswordResetToken.builder()
                .email(email)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        passwordResetTokenRepository.save(token);
        emailService.sendOtpEmail(email, otp);

        return AuthResponseDTO.builder()
                .message("OTP sent to your email")
                .build();
    }

    public AuthResponseDTO verifyResetOtp(String email, String otp) {
        PasswordResetToken token = passwordResetTokenRepository.findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new UnauthorizedException("Invalid OTP"));

        if (token.isUsed()) {
            throw new UnauthorizedException("OTP already used");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("OTP expired");
        }

        return AuthResponseDTO.builder()
                .message("OTP verified successfully")
                .build();
    }

    public AuthResponseDTO resetPassword(String email, String otp, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository.findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new UnauthorizedException("Invalid OTP"));

        if (token.isUsed()) {
            throw new UnauthorizedException("OTP already used");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("OTP expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        return AuthResponseDTO.builder()
                .message("Password reset successfully")
                .build();
    }

    public AuthResponseDTO changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UnauthorizedException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return AuthResponseDTO.builder()
                .message("Password updated successfully")
                .build();
    }
}