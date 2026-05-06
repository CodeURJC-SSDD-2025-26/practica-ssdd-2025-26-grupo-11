package es.codeurjc.practica2.security.jwt;

public record AuthResponse(Status status, String message) {
    public enum Status { SUCCESS, FAILURE }
}