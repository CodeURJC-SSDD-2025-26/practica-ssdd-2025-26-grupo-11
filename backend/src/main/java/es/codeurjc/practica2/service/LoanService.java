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

        return loans.stream().anyMatch(loan ->
                loan.getStatus() == Loan.Status.ACTIVO ||
                loan.getStatus() == Loan.Status.VENCIDO
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

        return loanRepository.save(loan);
    }
    public void markAsReturned(Long loanId) {
    Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

    loan.setStatus(Loan.Status.DEVUELTO);
    loan.setReturnDate(LocalDate.now());

    loanRepository.save(loan);
}
}