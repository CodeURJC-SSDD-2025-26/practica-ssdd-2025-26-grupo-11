package es.codeurjc.practica2.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UtilityClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendLoanEmail(String email, String user, String book, LocalDate date) {

        String url = "http://localhost:8081/api/email/loan-confirmation";

        Map<String, Object> request = Map.of(
                "toEmail", email,
                "userName", user,
                "bookTitle", book,
                "returnDate", date.toString()
        );

        restTemplate.postForEntity(url, request, Void.class);
    }
}