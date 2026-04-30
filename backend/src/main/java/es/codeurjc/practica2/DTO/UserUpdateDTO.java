package es.codeurjc.practica2.DTO;

public record UserUpdateDTO(
        String name,
        String surname,
        String email,
        String description
) {
}