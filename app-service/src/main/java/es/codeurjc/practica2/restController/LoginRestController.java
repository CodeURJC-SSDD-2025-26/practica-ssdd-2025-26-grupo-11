package es.codeurjc.practica2.restController;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.practica2.dto.UserCreateDTO;
import es.codeurjc.practica2.security.jwt.AuthResponse;
import es.codeurjc.practica2.security.jwt.LoginRequest;
import es.codeurjc.practica2.security.jwt.UserLoginService;
import es.codeurjc.practica2.service.UserService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginRestController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        return userLoginService.login(response, loginRequest);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "RefreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        return userLoginService.refresh(response, refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        return ResponseEntity.ok(
                new AuthResponse(AuthResponse.Status.SUCCESS, userLoginService.logout(response)));
    }
    // Añadir en LoginRestController.java, tras el @PostMapping("/login"):

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody UserCreateDTO dto) throws IOException {

        List<String> errors = userService.validateRegistration(
                dto.name(), dto.surname(), dto.email(), dto.password(), dto.confirmPassword());

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(AuthResponse.Status.FAILURE, String.join(" ", errors)));
        }

        userService.registerUser(dto.name(), dto.surname(), dto.email(), dto.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(AuthResponse.Status.SUCCESS, "User registered successfully"));
    }
}