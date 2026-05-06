package es.codeurjc.practica2.dto;

public record UserCreateDTO(
        String name,
        String surname,
        String email,
        String password,
        String confirmPassword
) {
}
