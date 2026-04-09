package es.codeurjc.practica2.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.User;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUser(User user);
    List<Loan> findByBook(Book book);

    List<Loan> findByUserAndBook(User user, Book book);
    
    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId")
    List<Loan> findByBookId(@Param("bookId") Long bookId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM loans WHERE book_id = :bookId AND status = 'DEVUELTO'", nativeQuery = true)
    void deleteReturnedLoansByBook(@Param("bookId") Long bookId);
    
    @Query(value = "SELECT COUNT(*) FROM loans WHERE book_id = :bookId AND (status = 'ACTIVO' OR status = 'VENCIDO')", nativeQuery = true)
    Long countActiveOrOverdueLoans(@Param("bookId") Long bookId);
}
