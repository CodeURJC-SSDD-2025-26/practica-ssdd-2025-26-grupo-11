package es.codeurjc.practica2.DTO;

public record BookDTO(
        Long id,
        String title,
        String author,
        String description,
        String genre,
        String genreDisplayName,
        float rating,
        int year,
        long isbn,
        boolean available,
        String imageUrl
) {
}