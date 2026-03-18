package es.codeurjc.practica2.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.model.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUser(User user);
    List<Review> findByBook(Book book);
}