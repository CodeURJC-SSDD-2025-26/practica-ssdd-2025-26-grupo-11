package es.codeurjc.practica2.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
