package com.abhilash.authsystem.controller;

import com.abhilash.authsystem.dto.LoginRequest;
import com.abhilash.authsystem.dto.RegisterRequest;
import com.abhilash.authsystem.entity.RefreshToken;
import com.abhilash.authsystem.service.AuthService;
import com.abhilash.authsystem.service.JwtService;
import com.abhilash.authsystem.service.RefreshTokenService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        String response = authService.register(request);
        if(response.equals("Already exists")){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User Already Exists");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("User Created");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        String response = authService.login(request);
        RefreshToken refToken = refreshTokenService.createRefreshToken(request.getEmail());
        if(response.equals("User doesn't exist")){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Incorrect Email ID");
        }else if(response.equals("Incorrect credentials")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect Password");
        }
        return ResponseEntity.ok(Map.of("token",response,"refAccessToken",refToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String,String> req){
        String requestToken = req.get("refreshToken");
        System.out.println("Abhilash requestToken is " + requestToken);

        RefreshToken rt = refreshTokenService.verifyToken(requestToken);

        String newAccessToken = jwtService.generateToken(rt.getUser().getEmail());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(rt.getUser().getEmail());

        refreshTokenService.deleteByUser(rt.getUser());

        System.out.println("Abhilash new access token is" + newAccessToken);
        System.out.println("Abhilash new newRefreshToken is" + newRefreshToken.getToken());

        return ResponseEntity.ok(
                Map.of(
                        "accessToken", newAccessToken,
                        "refreshToken", newRefreshToken.getToken()
                )
        );
    }
}
