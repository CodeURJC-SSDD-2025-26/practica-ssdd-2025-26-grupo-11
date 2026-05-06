package es.codeurjc.utility_service.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendLoanConfirmation(String toEmail, String userName, String bookTitle, LocalDate returnDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("biblioonlineconfirmacion@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Confirmación de préstamo - BiblioOnline");

            String formattedDate = returnDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String text = "¡Hola, " + userName + "!\n\n"
                    + "Te confirmamos que has tomado prestado el libro: \"" + bookTitle + "\".\n\n"
                    + "Fecha límite de devolución: " + formattedDate + ".\n\n"
                    + "¡Disfruta de tu lectura!\n"
                    + "El equipo de BiblioOnline.";

            message.setText(text);
            mailSender.send(message);
            logger.info("Email sent to: {}", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}