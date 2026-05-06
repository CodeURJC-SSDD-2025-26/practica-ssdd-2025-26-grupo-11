package es.codeurjc.utility_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.utility_service.dto.EmailRequest;
import es.codeurjc.utility_service.service.EmailService;

@RestController
@RequestMapping("/api/v1/emailService")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<Void> sendLoanEmail(@RequestBody EmailRequest request) {
        emailService.sendLoanConfirmation(
                request.toEmail(),
                request.userName(),
                request.bookTitle(),
                request.returnDate());
        return ResponseEntity.ok().build();
    }
}