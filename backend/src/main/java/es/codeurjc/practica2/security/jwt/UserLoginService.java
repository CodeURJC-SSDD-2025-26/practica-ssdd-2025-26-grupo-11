package es.codeurjc.practica2.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class UserLoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public ResponseEntity<AuthResponse> login(HttpServletResponse response, LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(), loginRequest.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtTokenProvider.generateToken(authentication, TokenType.ACCESS);
            String refreshToken = jwtTokenProvider.generateToken(authentication, TokenType.REFRESH);

            addCookie(response, TokenType.ACCESS, accessToken);
            addCookie(response, TokenType.REFRESH, refreshToken);

            return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Login successful"));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new AuthResponse(AuthResponse.Status.FAILURE, "Invalid credentials"));
        }
    }

    public ResponseEntity<AuthResponse> refresh(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401)
                    .body(new AuthResponse(AuthResponse.Status.FAILURE, "Invalid or expired refresh token"));
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        String newAccessToken = jwtTokenProvider.generateTokenFromUsername(username, TokenType.ACCESS);

        addCookie(response, TokenType.ACCESS, newAccessToken);

        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Token refreshed"));
    }

    public String logout(HttpServletResponse response) {
        clearCookie(response, TokenType.ACCESS);
        clearCookie(response, TokenType.REFRESH);
        SecurityContextHolder.clearContext();
        return "Logged out successfully";
    }

    private void addCookie(HttpServletResponse response, TokenType tokenType, String value) {
        Cookie cookie = new Cookie(tokenType.cookieName, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true en producción con HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) tokenType.duration.toSeconds());
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, TokenType tokenType) {
        Cookie cookie = new Cookie(tokenType.cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}