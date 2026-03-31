package es.codeurjc.practica2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.service.ReviewService;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @DeleteMapping("/review/{id}")
    public String deleteReview(@PathVariable Long id) {
        Review review = reviewService.findById(id);
        Long bookId = review.getBook().getId(); 
        reviewService.deleteById(id);
        return "redirect:/book-detail/" + bookId;
    }
}