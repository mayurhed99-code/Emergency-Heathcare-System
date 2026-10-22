package com.emergency.platform.controller.auth;

import com.emergency.platform.dto.AuthRequest;
import com.emergency.platform.dto.AuthResponse;
import com.emergency.platform.dto.OtpRequest;
import com.emergency.platform.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/driver/verify-otp")
    public AuthResponse verifyDriverOtp(@Valid @RequestBody OtpRequest request) {
        return authService.verifyDriverOtp(request);
    }
}
