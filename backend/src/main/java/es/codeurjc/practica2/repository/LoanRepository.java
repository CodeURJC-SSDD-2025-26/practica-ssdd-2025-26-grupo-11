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

    @Query("""
            SELECT l.book.genre, COUNT(l)
            FROM Loan l
            WHERE l.book IS NOT NULL
            GROUP BY l.book.genre
            """)
    List<Object[]> countLoansByGenre();

    long countByStatus(Loan.Status status);

    /**
     * Admin panel: search loans by user name/surname/email or book title,
     * and optionally filter by status. Null parameters are ignored.
     */
    @Query("""
            SELECT l FROM Loan l
            WHERE (
                :q IS NULL
                OR LOWER(l.user.name)    LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(l.user.surname) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(l.user.email)   LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(l.book.title)   LIKE LOWER(CONCAT('%', :q, '%'))
                OR CAST(l.id AS string)  LIKE CONCAT('%', :q, '%')
            )
            AND (:status IS NULL OR l.status = :status)
            ORDER BY l.id DESC
            """)
    List<Loan> searchLoans(@Param("q") String q,
            @Param("status") Loan.Status status);
}