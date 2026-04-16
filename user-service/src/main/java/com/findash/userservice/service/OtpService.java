package com.findash.userservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpService {

    private static final String OTP_PREFIX = "otp:";
    private static final String RESET_PREFIX = "reset:";
    private final StringRedisTemplate redis;
    private final long expirySeconds;

    public OtpService(StringRedisTemplate redis,
                      @Value("${app.otp.expiry-seconds}") long expirySeconds) {
        this.redis = redis;
        this.expirySeconds = expirySeconds;
    }

    /** Generate a 6-digit OTP, store in Redis with TTL, and return it */
    public String generateAndStore(String email) {
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));
        redis.opsForValue().set(OTP_PREFIX + email, otp,
                Duration.ofSeconds(expirySeconds));
        return otp;
    }

    /** Returns true if the provided OTP matches and is still valid, then deletes it */
    public boolean validateAndConsume(String email, String otp) {
        String key = OTP_PREFIX + email;
        String stored = redis.opsForValue().get(key);
        if (stored != null && stored.equals(otp)) {
            redis.delete(key);
            return true;
        }
        return false;
    }

    public String generateAndStoreReset(String email) {
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));
        redis.opsForValue().set(RESET_PREFIX + email, otp,
                Duration.ofSeconds(expirySeconds));
        return otp;
    }

    public boolean validateAndConsumeReset(String email, String otp) {
        String key = RESET_PREFIX + email;
        String stored = redis.opsForValue().get(key);
        if (stored != null && stored.equals(otp)) {
            redis.delete(key);
            return true;
        }
        return false;
    }
}
