package com.abhilash.authsystem.service;

import com.abhilash.authsystem.dto.LoginRequest;
import com.abhilash.authsystem.dto.RegisterRequest;
import com.abhilash.authsystem.entity.Role;
import com.abhilash.authsystem.entity.User;
import com.abhilash.authsystem.repository.RoleRepository;
import com.abhilash.authsystem.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public String register(RegisterRequest req) {
        String email = req.getEmail();
        String pass = req.getPassword();
        String name = req.getName();

        if(userRepository.findUserByEmail(email).isPresent()){
            throw new RuntimeException("User already exists");
        }

        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("Role not found"));

        User user = User.builder().email(email).password(passwordEncoder.encode(pass)).name(name).roles(Set.of(role)).build();
        userRepository.save(user);
        return "User Saved";
    }

    public String login(LoginRequest login){
        String email = login.getEmail();
        String pass = login.getPassword();
        Optional<User> user = null;

        if(userRepository.findUserByEmail(email).isEmpty()){
            throw new RuntimeException("User doesn't exist");
        }

        user = userRepository.findUserByEmail(email);

        if(passwordEncoder.matches(pass,user.get().getPassword())){
            return jwtService.generateToken(user.get().getEmail());
        }

        throw new RuntimeException("Invalid credentials");
    }

}