package com.findash.userservice.service;

import com.findash.userservice.dto.VerifyEmailRequest;
import com.findash.userservice.dto.ForgotPasswordRequest;
import com.findash.userservice.dto.ResetPasswordRequest;
import com.findash.userservice.dto.LoginRequest;
import com.findash.userservice.dto.RegisterRequest;
import com.findash.userservice.exception.UserAlreadyExistsException;
import com.findash.userservice.model.User;
import com.findash.userservice.repository.UserRepository;
import com.findash.userservice.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final EmailService emailService;

    // Constructor injection - Spring automatically provides these
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       OtpService otpService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    /**
     * Registers a new user into the PostgreSQL database, generates a secure OTP via Redis,
     * and dispatches a verification email for zero-trust authentication.
     *
     * @param request The user's registration payload (Name, Email, Raw Password)
     * @return A map containing success instructions for the frontend
     * @throws RuntimeException if the email is already in use or the database connection fails
     */
    @Transactional
    public Map<String, String> register(RegisterRequest request) {

        // Check if email already exists
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use.");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken.");
        }

        // Build the User entity
        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save to PostgreSQL
        userRepository.save(user);

        user.setEmailVerified(false);

        String otp = otpService.generateAndStore(request.getEmail());
        emailService.sendVerificationCode(request.getEmail(), otp);

        return Map.of("message", "Registration successful. Please verify your email.");
    }

    /**
     * Validates a 6-digit OTP against the ElastiCache Redis store to verify an email address.
     * Marks the user as 'verified' in PostgreSQL and issues a secure JWT.
     *
     * @param request The verification payload containing the Email and the OTP provided by the user
     * @return A map containing a newly minted JWT token for authorization
     * @throws RuntimeException if the OTP is invalid, expired, or the user does not exist
     */
    @Transactional
    public Map<String, String> verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified.");
        }

        boolean valid = otpService.validateAndConsume(request.getEmail(), request.getCode());
        if (!valid) {
            throw new RuntimeException("Invalid or expired verification code.");
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return Map.of(
                "token", token,
                "username", user.getUsername(),
                "name", user.getFullName(),
                "message", "Email verified successfully."
        );
    }

    /**
     * Initiates the password recovery pipeline by safely verifying user existence,
     * generating a time-limited OTP in Redis, and dispatching a recovery email.
     *
     * @param request The payload containing the user's registered email address
     * @return A map containing a generic success message to prevent email enumeration attacks
     */
    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        // Check user exists - don't reveal if email is registered or not (security best practice)
        boolean exists = userRepository.existsByEmail(request.getEmail());
        if (exists) {
            String otp = otpService.generateAndStoreReset(request.getEmail());
            emailService.sendPasswordResetCode(request.getEmail(), otp);
        }
        // Always return the same message regardless
        return Map.of("message", "If that email is registered, a reset code has been sent.");
    }

    /**
     * Completes the password recovery pipeline by validating the recovery OTP from Redis,
     * securely hashing the new password via BCrypt, and updating the database record.
     *
     * @param request The payload containing the Email, the recovery OTP, and the new Password
     * @return A map containing a success confirmation message
     * @throws RuntimeException if the OTP is invalid or expired
     */
    @Transactional
    public Map<String, String> resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid request."));

        boolean valid = otpService.validateAndConsumeReset(request.getEmail(), request.getCode());
        if (!valid) {
            throw new RuntimeException("Invalid or expired reset code.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return Map.of("message", "Password reset successfully. You can now log in.");
    }

    /**
     * Authenticates a user by verifying their email and BCrypt hashed password.
     * If the account is unverified, it triggers a new OTP dispatch.
     *
     * @param request The user's login payload (Email, Password)
     * @return A map containing the JWT token upon success, or verification instructions
     * @throws RuntimeException if the credentials are invalid or the user is not found
     */
    public Map<String, String> login(LoginRequest request) {

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        // Compare plain password with hashed password in DB
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        // Generate JWT Token
        String token = jwtUtil.generateToken(user.getEmail());

        return Map.of(
                "token", token,
                "username", user.getUsername(),
                "name", user.getFullName()
        );
    }
}
