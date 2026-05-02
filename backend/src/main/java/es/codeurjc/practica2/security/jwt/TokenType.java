package es.codeurjc.practica2.security.jwt;

import java.time.Duration;

public enum TokenType {

    ACCESS(Duration.ofMinutes(10), "AuthToken"),
    REFRESH(Duration.ofDays(7), "RefreshToken");

    public final Duration duration;
    public final String cookieName;

    TokenType(Duration duration, String cookieName) {
        this.duration = duration;
        this.cookieName = cookieName;
    }
}