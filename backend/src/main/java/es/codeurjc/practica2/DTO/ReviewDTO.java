package es.codeurjc.practica2.DTO;

public record ReviewDTO(
        Long id,
        String comment,
        Integer rating,
        Long userId,
        String userName,
        Long bookId,
        String bookTitle
) {
}
