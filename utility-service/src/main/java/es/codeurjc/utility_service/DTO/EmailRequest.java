package main.java.es.codeurjc.utility_service.DTO;

public record EmailRequest(
    String toEmail,
    String userName,
    String bookTitle,
    LocalDate returnDate
) {}