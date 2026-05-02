package es.codeurjc.practica2.restController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.practica2.security.jwt.AuthResponse;
import es.codeurjc.practica2.security.jwt.LoginRequest;
import es.codeurjc.practica2.security.jwt.UserLoginService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginRestController {

    @Autowired
    private UserLoginService userLoginService;

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
}