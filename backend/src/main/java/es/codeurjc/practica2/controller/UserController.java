package es.codeurjc.practica2.controller;

import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.LoanRepository;
import es.codeurjc.practica2.repository.ReviewRepository;
import es.codeurjc.practica2.repository.UserRepository;
import es.codeurjc.practica2.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookService bookService;

    @GetMapping("/base")
    public String base(Model model, HttpServletRequest request) {

        String email = request.getUserPrincipal().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("featuredBooks", bookService.findTopRatedBooks());
        model.addAttribute("user", user);
        model.addAttribute("loans", user.getLoans());
        model.addAttribute("name", user.getName());

        if (user.getLoans() == null || user.getLoans().isEmpty()) {
            model.addAttribute("recommendedBooks", null);
        } else {
            model.addAttribute("recommendedBooks", List.of()); //Aquí iría la query (userService.getRecommendedBooks(user) por ejemplo) 
        }

        return "base";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Loan> recentLoans = loanRepository.findByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("totalLoans", recentLoans.size());
        model.addAttribute("totalReviews", reviewRepository.findByUser(user).size());
        model.addAttribute("activeLoans", recentLoans.stream().count());
        model.addAttribute("recentLoans", recentLoans);

        return "profile";
    }
}