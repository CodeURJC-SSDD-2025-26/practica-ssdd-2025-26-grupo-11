package es.codeurjc.practica2.service;
 
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
 
import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.LoanRepository;
 
@Service
public class LoanService {
 
    @Autowired
    private LoanRepository loanRepository;
 
    @Autowired
    private EmailService emailService;
 
    @Lazy
    @Autowired
    private BookService bookService;
 
    // -------------------------------------------------------------------------
    // Basic CRUD
    // -------------------------------------------------------------------------
 
    public List<Loan> findAll() {
        return loanRepository.findAll();
    }
 
    public Optional<Loan> findById(Long id) {
        return loanRepository.findById(id);
    }
 
    public List<Loan> findByUser(User user) {
        return loanRepository.findByUser(user);
    }
 
    public Loan save(Loan loan) {
        return loanRepository.save(loan);
    }
 
    public void deleteById(Long id) {
        loanRepository.deleteById(id);
    }
 
    // -------------------------------------------------------------------------
    // Availability checks
    // -------------------------------------------------------------------------
 
    public boolean hasActiveOrOverdueLoan(User user, Book book) {
        List<Loan> loans = loanRepository.findByUserAndBook(user, book);
        return loans.stream().anyMatch(loan ->
                loan.getStatus() == Loan.Status.ACTIVO
                        || loan.getStatus() == Loan.Status.VENCIDO);
    }
 
    public boolean isBookAvailable(Book book) {
        List<Loan> loans = loanRepository.findByBook(book);
        return loans.stream().noneMatch(loan ->
                loan.getStatus() == Loan.Status.ACTIVO
                        || loan.getStatus() == Loan.Status.VENCIDO);
    }
 
    /**
     * Deletes a book only if it has no active or overdue loans.
     * Returns true if deletion succeeded, false if blocked by active loans.
     */
    public boolean deleteBookIfAllowed(Long bookId) {
        if (loanRepository.countActiveOrOverdueLoans(bookId) > 0) {
            return false;
        }
        loanRepository.deleteReturnedLoansByBook(bookId);
        bookService.deleteById(bookId);
        return true;
    }
 
    // -------------------------------------------------------------------------
    // Loan creation (unchanged)
    // -------------------------------------------------------------------------
 
    public Loan createLoan(User user, Book book) {
        Loan loan = new Loan(
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                Loan.Status.ACTIVO,
                user,
                book
        );
 
        Loan savedLoan = loanRepository.save(loan);
 
        emailService.sendLoanConfirmation(
                user.getEmail(),
                user.getName(),
                book.getTitle(),
                savedLoan.getReturnDate()
        );
 
        return savedLoan;
    }
 
    // -------------------------------------------------------------------------
    // Status transitions (moved from LoanController / AdminController)
    // -------------------------------------------------------------------------
 
    /**
     * Marks a loan as returned by the owner user.
     * Throws an exception if the loan does not belong to the requesting user.
     */
    public void returnLoanByUser(Long loanId, User requestingUser) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
 
        if (!loan.getUser().getId().equals(requestingUser.getId())) {
            throw new RuntimeException("Access denied: loan belongs to another user");
        }
 
        loan.setStatus(Loan.Status.DEVUELTO);
        loan.setReturnDate(LocalDate.now());
        loanRepository.save(loan);
    }
 
    /**
     * Marks a loan as returned by an admin (no ownership check).
     */
    public void markAsReturned(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
 
        loan.setStatus(Loan.Status.DEVUELTO);
        loan.setReturnDate(LocalDate.now());
        loanRepository.save(loan);
    }
 
    public void updateLoanStatus(Long id, Loan.Status status) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
 
        loan.setStatus(status);
        if (status == Loan.Status.DEVUELTO) {
            loan.setReturnDate(LocalDate.now());
        }
        loanRepository.save(loan);
    }
 
    // -------------------------------------------------------------------------
    // Admin: update loan dates & status (moved from AdminController)
    // -------------------------------------------------------------------------
 
    /**
     * Validates and updates loan dates and status from the admin edit form.
     * Returns a list of validation errors. Empty list means success.
     */
    public List<String> validateAndUpdateLoan(Long id, LocalDate loanDate, LocalDate returnDate,
            String status) {
 
        List<String> errors = new ArrayList<>();
 
        if (loanDate == null) {
            errors.add("La fecha de préstamo es obligatoria.");
        }
        if (returnDate == null) {
            errors.add("La fecha de devolución es obligatoria.");
        }
        if (loanDate != null && returnDate != null && returnDate.isBefore(loanDate)) {
            errors.add("La fecha de devolución no puede ser anterior a la fecha de préstamo.");
        }
        if (loanDate != null && loanDate.isAfter(LocalDate.now())) {
            errors.add("La fecha de préstamo no puede ser futura.");
        }
 
        boolean validStatus = false;
        for (Loan.Status s : Loan.Status.values()) {
            if (s.name().equals(status)) {
                validStatus = true;
                break;
            }
        }
        if (!validStatus) {
            errors.add("El estado seleccionado no es válido.");
        }
 
        if (!errors.isEmpty()) {
            return errors;
        }
 
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
 
        Loan.Status selectedStatus = Loan.Status.valueOf(status);
        loan.setLoanDate(loanDate);
        loan.setReturnDate(returnDate);
 
        if (selectedStatus == Loan.Status.DEVUELTO) {
            loan.setStatus(Loan.Status.DEVUELTO);
        } else {
            loan.setStatus(Loan.Status.ACTIVO);
            loan.refreshStatusFromDates();
        }
 
        loanRepository.save(loan);
        return errors; // empty
    }
 
    // -------------------------------------------------------------------------
    // Stats / charts (used by AdminController)
    // -------------------------------------------------------------------------
 
    public List<Object[]> countLoansByGenre() {
        return loanRepository.countLoansByGenre();
    }
 
    public long countByStatus(Loan.Status status) {
        return loanRepository.countByStatus(status);
    }
 
    // -------------------------------------------------------------------------
    // My-loans summary (moved from LoanController)
    // -------------------------------------------------------------------------
 
    /**
     * Refreshes statuses for all non-returned loans of a user and returns them.
     */
    public List<Loan> getLoansForUser(User user) {
        List<Loan> loans = loanRepository.findByUser(user);
        for (Loan loan : loans) {
            if (loan.getStatus() != Loan.Status.DEVUELTO) {
                loan.refreshStatusFromDates();
            }
        }
        return loans;
    }
}
 