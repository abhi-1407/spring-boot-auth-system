package com.abhilash.authsystem.controller;

import com.abhilash.authsystem.dto.LoginRequest;
import com.abhilash.authsystem.dto.RegisterRequest;
import com.abhilash.authsystem.dto.RoleAssignRequest;
import com.abhilash.authsystem.entity.RefreshToken;
import com.abhilash.authsystem.entity.Role;
import com.abhilash.authsystem.entity.User;
import com.abhilash.authsystem.repository.RoleRepository;
import com.abhilash.authsystem.repository.UserRepository;
import com.abhilash.authsystem.service.AuthService;
import com.abhilash.authsystem.service.JwtService;
import com.abhilash.authsystem.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletResponse response) {

        String accessToken = authService.login(request);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(request.getEmail());

        Cookie cookie = new Cookie("refreshToken", refreshToken.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // change to true in production
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

        return ResponseEntity.ok(
                Map.of("accessToken", accessToken)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request,
                                     HttpServletResponse response) {

        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken == null) {
            throw new RuntimeException("Refresh token missing");
        }

        RefreshToken rt = refreshTokenService.verifyToken(refreshToken);

        String newAccessToken =
                jwtService.generateToken(rt.getUser().getEmail());

        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(rt.getUser().getEmail());

        //Rotation of the cookie
        Cookie newCookie = new Cookie("refreshToken", newRefreshToken.getToken());
        newCookie.setHttpOnly(true);
        newCookie.setSecure(false); // ⚠️ true in prod
        newCookie.setPath("/api/v1/auth");
        newCookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(newCookie);

        refreshTokenService.deleteByUser(rt.getUser());

        return ResponseEntity.ok(
                Map.of("accessToken", newAccessToken)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response) {

        String refreshToken = extractRefreshTokenFromCookies(request);

        if (refreshToken != null) {
            RefreshToken rt = refreshTokenService.verifyToken(refreshToken);
            refreshTokenService.deleteByUser(rt.getUser());
        }

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/assign-role")
    public ResponseEntity<?> assignRole(@RequestBody RoleAssignRequest req) {

        User user = userRepository.findUserByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(req.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);
        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "Role assigned successfully")
        );
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}