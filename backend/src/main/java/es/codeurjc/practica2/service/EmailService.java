package es.codeurjc.practica2.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendLoanConfirmation(String toEmail, String userName, String bookTitle, LocalDate returnDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            message.setFrom("biblioonline@noreply.com"); 
            message.setTo(toEmail);
            message.setSubject("Confirmación de préstamo - BiblioOnline");

            String formattedDate = returnDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            String text = "¡Hola, " + userName + "!\n\n"
                    + "Te confirmamos que has tomado prestado con éxito el libro: \"" + bookTitle + "\".\n\n"
                    + "Recuerda que tu fecha límite de devolución es el: " + formattedDate + ".\n\n"
                    + "¡Disfruta de tu lectura!\n"
                    + "El equipo de BiblioOnline.";

            message.setText(text);
            mailSender.send(message);
            
            System.out.println("Correo enviado con éxito a: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Error al enviar el correo de confirmación: " + e.getMessage());
        }
    }
}