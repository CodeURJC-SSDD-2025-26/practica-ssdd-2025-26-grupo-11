package main.java.es.codeurjc.utility_service.controller;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/loan-confirmation")
    public ResponseEntity<Void> sendLoanEmail(@RequestBody EmailRequest request) {

        emailService.sendLoanConfirmation(
                request.toEmail(),
                request.userName(),
                request.bookTitle(),
                request.returnDate()
        );

        return ResponseEntity.ok().build();
    }
}