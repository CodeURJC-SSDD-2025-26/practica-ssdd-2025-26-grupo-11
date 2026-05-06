package es.codeurjc.practica2.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class UtilityClient {

    private final RestTemplate restTemplate;

    public UtilityClient() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);

        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
        this.restTemplate.getMessageConverters().add(0, converter);
    }

    public void sendLoanEmail(String email, String user, String book, LocalDate date) {
        String url = "http://localhost:8081/api/email/loan-confirmation";

        Map<String, Object> request = Map.of(
                "toEmail", email,
                "userName", user,
                "bookTitle", book,
                "returnDate", date.toString()); // ISO format: "2025-06-01"

        try {
            restTemplate.postForEntity(url, request, Void.class);
        } catch (Exception e) {
            System.err.println("Could not send email: " + e.getMessage());
        }
    }
}