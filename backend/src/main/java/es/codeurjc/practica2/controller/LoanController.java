package es.codeurjc.practica2.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.service.LoanService;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin
public class LoanController {

    @Autowired
    private LoanService loanService;

    @GetMapping
    public List<Loan> getAllLoans() {
        return loanService.findAll();
    }

    @GetMapping("/{id}")
    public Loan getLoanById(@PathVariable Long id) {
        return loanService.findById(id).orElse(null);
    }

    @PostMapping
    public Loan createLoan(@RequestBody Loan loan) {
        return loanService.save(loan);
    }

    @DeleteMapping("/{id}")
    public void deleteLoan(@PathVariable Long id) {
        loanService.deleteById(id);
    }
}
