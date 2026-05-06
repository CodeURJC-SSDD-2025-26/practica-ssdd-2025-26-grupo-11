package es.codeurjc.practica2.service;

import java.time.LocalDate;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UtilityClient {

    private static final Logger logger = LoggerFactory.getLogger(UtilityClient.class);

    private final RestTemplate restTemplate;

    @Value("${utility.service.url}")
    private String utilityServiceUrl;

    public UtilityClient() {
        this.restTemplate = new RestTemplate();
    }

    public void sendLoanEmail(String email, String user, String book, LocalDate date) {
        String url = utilityServiceUrl + "/api/v1/emails";

        Map<String, Object> request = Map.of(
                "toEmail", email,
                "userName", user,
                "bookTitle", book,
                "returnDate", date.toString()
        );

        try {
            restTemplate.postForEntity(url, request, Void.class);
        } catch (Exception e) {
            logger.warn("Could not send loan confirmation email: {}", e.getMessage());
        }
    }
}