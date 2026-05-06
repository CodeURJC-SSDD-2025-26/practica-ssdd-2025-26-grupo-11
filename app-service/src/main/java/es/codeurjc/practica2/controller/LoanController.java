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
import es.codeurjc.practica2.service.LoanService;
import es.codeurjc.practica2.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
 
@Controller
public class LoanController {
 
    @Autowired
    private LoanService loanService;
 
    @Autowired
    private UserService userService;
 
    @GetMapping("/my-loans")
    public String myLoans(Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        List<Loan> loans = loanService.getLoansForUser(user);
 
        long activeLoans  = loans.stream().filter(Loan::isActive).count();
        long overdueLoans = loans.stream().filter(Loan::isOverdue).count();
        long returnedLoans = loans.stream().filter(Loan::isReturned).count();
 
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
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        loanService.returnLoanByUser(id, user);
        return "redirect:/my-loans";
    }
}