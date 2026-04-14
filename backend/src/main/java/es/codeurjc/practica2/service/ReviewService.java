package es.codeurjc.practica2.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.BookRepository;
import es.codeurjc.practica2.repository.ReviewRepository;
import es.codeurjc.practica2.repository.UserRepository;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    public Review addReview(Long bookId, Long userId, String comment, int rating) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        Review review = new Review(comment, rating, user, book);
        Review savedReview = reviewRepository.save(review);

        updateBookRating(book);

        return savedReview;
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id).orElseThrow();
    }

    public void deleteById(Long id) {
        Review review = reviewRepository.findById(id).orElseThrow();
        Book book = review.getBook();

        reviewRepository.deleteById(id);

        updateBookRating(book);
    }

    private void updateBookRating(Book book) {
        List<Review> reviews = reviewRepository.findByBook(book);

        if (reviews.isEmpty()) {
            book.setRating(0);
        } else {
            double average = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            double rounded = Math.round(average * 10.0) / 10.0;

            book.setRating((float) rounded);
        }

        bookRepository.save(book);
    }
}
