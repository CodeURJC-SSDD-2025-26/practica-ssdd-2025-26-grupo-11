package es.codeurjc.practica2.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.UserRepository;
import es.codeurjc.practica2.repository.LoanRepository;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoanController {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my-loans")
    public String myLoans(Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Loan> loans = loanRepository.findByUser(user);
        for (Loan loan : loans) {
            if (loan.getStatus() != Loan.Status.DEVUELTO) {
                loan.refreshStatusFromDates();
            }
}

        long activeLoans = loans.stream()
                .filter(Loan::isActive)
                .count();

        long overdueLoans = loans.stream()
                .filter(Loan::isOverdue)
                .count();

        long returnedLoans = loans.stream()
                .filter(Loan::isReturned)
                .count();

        model.addAttribute("user", user);
        model.addAttribute("loans", loans);
        model.addAttribute("activeLoansCount", activeLoans);
        model.addAttribute("overdueLoansCount", overdueLoans);
        model.addAttribute("returnedLoansCount", returnedLoans);
        model.addAttribute("totalLoansCount", loans.size());

        return "my-loans";
    }

    @PostMapping("/my-loans/return/{id}")
    public String returnLoan(@PathVariable Long id, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (!loan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permisos para modificar este préstamo");
        }

        loan.setStatus(Loan.Status.DEVUELTO);
        loan.setReturnDate(java.time.LocalDate.now());
        loanRepository.save(loan);

        return "redirect:/my-loans";
    }

}
