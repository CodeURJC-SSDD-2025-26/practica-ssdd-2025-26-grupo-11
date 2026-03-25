package es.codeurjc.practica2.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.practica2.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
   List<Book> findTop4ByOrderByRatingDesc();
}
