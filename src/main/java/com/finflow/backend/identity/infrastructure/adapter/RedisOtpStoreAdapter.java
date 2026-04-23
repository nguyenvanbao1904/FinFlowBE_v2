package com.finflow.backend.identity.infrastructure.adapter;

import com.finflow.backend.common.infrastructure.redis.RedisService;
import com.finflow.backend.identity.application.dto.OtpData;
import com.finflow.backend.identity.application.port.out.OtpStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisOtpStoreAdapter implements OtpStorePort {

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String OTP_RATE_KEY_PREFIX = "otp:rate:";

    private final RedisService redisService;

    @Override
    public boolean isRateLimited(String email) {
        return redisService.exists(OTP_RATE_KEY_PREFIX + email);
    }

    @Override
    public void markRateLimited(String email, long seconds) {
        redisService.set(OTP_RATE_KEY_PREFIX + email, "1", seconds, TimeUnit.SECONDS);
    }

    @Override
    public void saveOtp(String email, OtpData otpData, long minutes) {
        redisService.set(OTP_KEY_PREFIX + email, otpData, minutes, TimeUnit.MINUTES);
    }

    @Override
    public OtpData getOtp(String email) {
        return redisService.get(OTP_KEY_PREFIX + email, OtpData.class);
    }

    @Override
    public void deleteOtp(String email) {
        redisService.delete(OTP_KEY_PREFIX + email);
    }
}
