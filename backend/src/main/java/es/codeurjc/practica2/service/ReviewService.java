package es.codeurjc.practica2.service;

import java.util.List;
import java.util.Optional;

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

    // -------------------------------------------------------------------------
    // Basic queries
    // -------------------------------------------------------------------------

    public Review findById(Long id) {
        return reviewRepository.findById(id).orElseThrow();
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    /**
     * Returns the review left by a specific user on a specific book, if any.
     * Used by BookController to know whether the user can still leave a review.
     */
    public Optional<Review> findByUserAndBook(User user, Book book) {
        return reviewRepository.findByUserAndBook(user, book);
    }

    /**
     * Returns the total number of reviews written by a user.
     * Used by UserController / profile page.
     */
    public long countByUser(User user) {
        return reviewRepository.findByUser(user).size();
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    public Review addReview(Long bookId, Long userId, String comment, int rating) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
        }

        if (comment == null || comment.isBlank()) {
            throw new IllegalArgumentException("El comentario es obligatorio");
        }

        if (comment.length() > 1500) {
            throw new IllegalArgumentException("El comentario no puede exceder 1500 caracteres");
        }

        if (reviewRepository.findByUserAndBook(user, book).isPresent()) {
            throw new IllegalStateException("El usuario ya ha escrito una reseña a este libro");
        }

        Review review = new Review(comment, rating, user, book);
        Review savedReview = reviewRepository.save(review);

        updateBookRating(book);

        return savedReview;
    }

    public void deleteById(Long id) {
        Review review = reviewRepository.findById(id).orElseThrow();
        Book book = review.getBook();

        reviewRepository.deleteById(id);
        updateBookRating(book);
    }

    /**
     * Deletes all reviews from a user and recalculates ratings for affected books.
     * Called when an admin deletes a user account.
     */
    public void deleteUserReviews(User user) {
        List<Review> userReviews = reviewRepository.findByUser(user);

        java.util.Set<Book> booksToUpdate = new java.util.HashSet<>();
        for (Review review : userReviews) {
            booksToUpdate.add(review.getBook());
        }

        reviewRepository.deleteAll(userReviews);

        for (Book book : booksToUpdate) {
            updateBookRating(book);
        }
    }

    // -------------------------------------------------------------------------
    // Private helper
    // -------------------------------------------------------------------------

    private void updateBookRating(Book book) {
        List<Review> reviews = reviewRepository.findByBook(book);

        if (reviews.isEmpty()) {
            book.setRating(0);
        } else {
            double average = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            book.setRating((float) (Math.round(average * 10.0) / 10.0));
        }

        bookRepository.save(book);
    }
}
