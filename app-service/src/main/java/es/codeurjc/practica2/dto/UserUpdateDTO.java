package es.codeurjc.practica2.dto;

public record UserUpdateDTO(
        String name,
        String surname,
        String email,
        String description
) {
}