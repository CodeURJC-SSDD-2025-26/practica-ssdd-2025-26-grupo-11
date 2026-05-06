package es.codeurjc.practica2.dto;

public record BookRequestDTO(
        String title,
        String author,
        String description,
        String genre,
        Integer year,
        Long isbn
) {
}