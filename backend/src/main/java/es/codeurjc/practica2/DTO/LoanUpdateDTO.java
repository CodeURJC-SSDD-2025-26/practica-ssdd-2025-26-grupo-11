package es.codeurjc.practica2.DTO;
import java.util.Date;

public record LoanUpdateDTO(
    Date loanDate,
    Date returnDate,
    String status
){
}
