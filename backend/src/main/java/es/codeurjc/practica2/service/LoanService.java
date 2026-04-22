package es.codeurjc.practica2.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
    private EmailService emailService; // <-- AÑADE ESTO

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

    public boolean hasActiveOrOverdueLoan(User user, Book book) {
        List<Loan> loans = loanRepository.findByUserAndBook(user, book);

        return loans.stream().anyMatch(loan
                -> loan.getStatus() == Loan.Status.ACTIVO
                || loan.getStatus() == Loan.Status.VENCIDO
        );
    }

    public boolean isBookAvailable(Book book) {
        List<Loan> loans = loanRepository.findByBook(book);

        return loans.stream().noneMatch(loan
                -> loan.getStatus() == Loan.Status.ACTIVO
                || loan.getStatus() == Loan.Status.VENCIDO
        );
    }

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

    public void markAsReturned(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        loan.setStatus(Loan.Status.DEVUELTO);
        loan.setReturnDate(LocalDate.now());

        loanRepository.save(loan);
    }

    public void updateLoanStatus(Long id, Loan.Status status) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        loan.setStatus(status);

        if (status == Loan.Status.DEVUELTO) {
            loan.setReturnDate(LocalDate.now());
        }

        loanRepository.save(loan);
    }

    public List<Object[]> countLoansByGenre() {
        return loanRepository.countLoansByGenre();
    }

    public long countByStatus(Loan.Status status) {
        return loanRepository.countByStatus(status);
    }
}
