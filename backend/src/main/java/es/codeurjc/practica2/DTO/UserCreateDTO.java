package es.codeurjc.practica2.DTO;

public record UserCreateDTO(
        String name,
        String surname,
        String email,
        String password,
        String confirmPassword
) {
}
