package es.codeurjc.practica2.dto;

public record UserDTO(
        Long id,
        String name,
        String surname,
        String email,
        String description,
        String imageUrl
) {
}