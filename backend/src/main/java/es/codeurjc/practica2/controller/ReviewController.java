package es.codeurjc.practica2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.UserRepository;
import es.codeurjc.practica2.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/book-detail/{bookId}/review")
    public String addReview(
            @PathVariable Long bookId,
            @RequestParam String comment,
            @RequestParam int rating,
            HttpServletRequest request) {

        String email = request.getUserPrincipal().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        reviewService.addReview(bookId, user.getId(), comment, rating);

        return "redirect:/book-detail/" + bookId;
    }

    @DeleteMapping("/review/{id}")
    public String deleteReview(@PathVariable Long id, HttpServletRequest request) {
        Review review = reviewService.findById(id);
        Long bookId = review.getBook().getId();

        String email = request.getUserPrincipal().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean isOwner = review.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = request.isUserInRole("ADMIN");

        if (!isOwner && !isAdmin) {
            // Returns 403 or redirects with error
            return "redirect:/book-detail/" + bookId + "?error=forbidden";
        }

        reviewService.deleteById(id);
        return "redirect:/book-detail/" + bookId;
    }
}