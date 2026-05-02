package es.codeurjc.practica2.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findTop4ByOrderByRatingDesc();

    List<Book> findByGenreOrderByRatingDesc(Genre genre);

    @Query("""
            SELECT b FROM Book b
            WHERE (
                :q IS NULL
                OR LOWER(b.title)  LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            AND (:genre IS NULL OR b.genre = :genre)
            ORDER BY b.title ASC
            """)
    List<Book> searchBooks(@Param("q") String q, @Param("genre") Genre genre);

    /**
     * Pageable version used by the admin panel.
     */
    @Query(value = """
            SELECT b FROM Book b
            WHERE (
                :q IS NULL
                OR LOWER(b.title)  LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            AND (:genre IS NULL OR b.genre = :genre)
            """,
            countQuery = """
            SELECT COUNT(b) FROM Book b
            WHERE (
                :q IS NULL
                OR LOWER(b.title)  LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            AND (:genre IS NULL OR b.genre = :genre)
            """)
    Page<Book> searchBooksPage(@Param("q") String q,
            @Param("genre") Genre genre,
            Pageable pageable);

    @Query("SELECT b.genre, AVG(b.rating) FROM Book b GROUP BY b.genre")
    List<Object[]> avgRatingByGenre();
}