package es.codeurjc.practica2.service;

import java.time.LocalDate;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class UtilityClient {

    private static final Logger logger = LoggerFactory.getLogger(UtilityClient.class);

    private final RestTemplate restTemplate;

    @Value("${utility.service.url}")
    private String utilityServiceUrl;

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
        String url = utilityServiceUrl + "/api/v1/emailService";

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