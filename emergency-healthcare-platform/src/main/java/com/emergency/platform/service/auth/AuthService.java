package com.emergency.platform.service.auth;

import com.emergency.platform.dto.AuthRequest;
import com.emergency.platform.dto.AuthResponse;
import com.emergency.platform.dto.OtpRequest;
import com.emergency.platform.entity.AppUser;
import com.emergency.platform.entity.Driver;
import com.emergency.platform.entity.Role;
import com.emergency.platform.exception.WorkflowException;
import com.emergency.platform.repository.AppUserRepository;
import com.emergency.platform.repository.DriverRepository;
import com.emergency.platform.security.JwtService;
import com.emergency.platform.service.notification.SmsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final SmsService smsService;

    public AuthService(AppUserRepository userRepository,
                       DriverRepository driverRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       OtpService otpService,
                       SmsService smsService) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.smsService = smsService;
    }

    public AuthResponse login(AuthRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new WorkflowException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new WorkflowException("Invalid credentials");
        }
        if (user.getRole() == Role.DRIVER) {
            Driver driver = driverRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new WorkflowException("Driver profile not found"));
            String otp = otpService.createOtp(user.getEmail(), "DRIVER_LOGIN");
            smsService.sendOtpSms(driver.getPhone(), otp);
            return new AuthResponse(null, user.getRole().name(), user.getName(), true, "OTP sent to driver phone");
        }
        return new AuthResponse(jwtService.generateToken(user), user.getRole().name(), user.getName(), false, "Authenticated");
    }

    public AuthResponse verifyDriverOtp(OtpRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new WorkflowException("Invalid credentials"));
        if (user.getRole() != Role.DRIVER) {
            throw new WorkflowException("OTP login is only required for drivers");
        }
        otpService.verify(request.email(), "DRIVER_LOGIN", request.otp());
        return new AuthResponse(jwtService.generateToken(user), user.getRole().name(), user.getName(), false, "Authenticated");
    }
}
