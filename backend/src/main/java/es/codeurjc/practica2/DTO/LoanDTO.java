package es.codeurjc.practica2.DTO;
import es.codeurjc.practica2.model.Loan.Status;

public record LoanDTO(
        Long id,
        Status status,
        String loanDate,
        String returnDate,
        String userName,
        String userEmail,
        String bookTitle
) {
}