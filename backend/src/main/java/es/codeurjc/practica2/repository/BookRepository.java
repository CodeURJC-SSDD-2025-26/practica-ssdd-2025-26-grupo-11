package es.codeurjc.practica2.repository;

import java.util.List;
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
         WHERE (:q IS NULL OR
                LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%')))
         AND (:genre IS NULL OR b.genre = :genre)
         """)
   List<Book> searchBooks(@Param("q") String q,
         @Param("genre") Genre genre);
}
