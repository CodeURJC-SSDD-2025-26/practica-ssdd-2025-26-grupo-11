package es.codeurjc.practica2.dto;
import java.time.LocalDate;

public record LoanDTO(
        Long id,
        LocalDate loanDate,
        LocalDate returnDate,
        String status,
        Long userId,
        String userName,
        String userEmail,
        Long bookId,
        String bookTitle
) {
}