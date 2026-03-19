package es.codeurjc.practica2.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.User;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUser(User user);
    List<Loan> findByBook(Book book);
}
