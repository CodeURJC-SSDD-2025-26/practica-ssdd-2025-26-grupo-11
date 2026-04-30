package es.codeurjc.practica2.dto;
import java.time.LocalDate;

public record LoanUpdateDTO(
    LocalDate loanDate,
    LocalDate returnDate,
    String status
){
}
