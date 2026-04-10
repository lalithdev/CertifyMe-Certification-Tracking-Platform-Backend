package com.certifyme.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authentication operations (login, register, OTP flow).
 *
 * <p>Standard login/register returns {@code token} + {@code user}.
 * Admin OTP flow returns {@code otpRequired} + timer fields with null token/user.
 *
 * <p>Null OTP-related fields are excluded from JSON output for cleaner responses
 * on non-admin flows via {@link JsonInclude}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {

    private String token;

    private UserResponseDTO user;

    private Boolean otpRequired;

    private Long remainingValiditySeconds;

    private Long resendCooldownSeconds;

    /**
     * Convenience factory method for standard (non-OTP) authentication responses.
     * Used after successful login or registration.
     *
     * @param token JWT access token
     * @param user  authenticated user details
     * @return AuthResponseDTO with otpRequired set to false
     */
    public static AuthResponseDTO ofSuccess(String token, UserResponseDTO user) {
        return AuthResponseDTO.builder()
                .token(token)
                .user(user)
                .otpRequired(false)
                .build();
    }
}
