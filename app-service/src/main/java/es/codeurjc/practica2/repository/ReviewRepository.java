package es.codeurjc.practica2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.model.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByUser(User user);

    List<Review> findByBook(Book book);

    Optional<Review> findByUserAndBook(User user, Book book);

    @Query("""
            SELECT r FROM Review r
            WHERE (
                :q IS NULL
                OR LOWER(r.user.name)    LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(r.user.surname) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(r.book.title)   LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(r.comment)      LIKE LOWER(CONCAT('%', :q, '%'))
            )
            ORDER BY r.id DESC
            """)
    List<Review> searchReviews(@Param("q") String q);

    /**
     * Pageable version used by the admin panel.
     */
    @Query(value = """
            SELECT r FROM Review r
            WHERE (
                :q IS NULL
                OR LOWER(r.user.name)    LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(r.user.surname) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(r.book.title)   LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(r.comment)      LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """,
            countQuery = """
            SELECT COUNT(r) FROM Review r
            WHERE (
                :q IS NULL
                OR LOWER(r.user.name)    LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(r.user.surname) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(r.book.title)   LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(r.comment)      LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """)
    Page<Review> searchReviewsPage(@Param("q") String q, Pageable pageable);
}