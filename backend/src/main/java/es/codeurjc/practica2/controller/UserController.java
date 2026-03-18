package es.codeurjc.practica2.controller;

import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.LoanRepository;
import es.codeurjc.practica2.repository.ReviewRepository;
import es.codeurjc.practica2.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
    
@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping("/profile")
    public String profile(Model model, HttpServletRequest request) {
        String name = request.getUserPrincipal().getName();
        User user = userRepository.findByName(name).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Loan> recentLoans = loanRepository.findByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("totalLoans", recentLoans.size());
        model.addAttribute("totalReviews", reviewRepository.findByUser(user).size());
        model.addAttribute("activeLoans", recentLoans.stream().count()); //filter(l -> l.getReturnDate() == null)
        model.addAttribute("recentLoans", recentLoans);

        return "profile";
    }
}