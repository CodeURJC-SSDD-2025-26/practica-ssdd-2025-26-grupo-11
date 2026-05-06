package es.codeurjc.utility_service.dto;

import java.time.LocalDate;

public record EmailRequest(
    String toEmail,
    String userName,
    String bookTitle,
    LocalDate returnDate
) {}