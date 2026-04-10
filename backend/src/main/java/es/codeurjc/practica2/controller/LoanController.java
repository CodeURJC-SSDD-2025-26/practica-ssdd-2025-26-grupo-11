package es.codeurjc.practica2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.repository.LoanRepository;

@Controller
public class LoanController {

    @Autowired
    private LoanRepository loanRepository;

    @GetMapping("/admin/edit-loans/{id}")
    public String editLoanForm(@PathVariable Long id, Model model) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        model.addAttribute("loan", loan);
        model.addAttribute("statuses", Loan.Status.values());

        return "admin/admin-edit-loans";
    }

    @PostMapping("/admin/edit-loans/{id}")
    public String updateLoan(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam("loanDate") java.time.LocalDate loanDate,
            @RequestParam("returnDate") java.time.LocalDate returnDate) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (returnDate.isBefore(loanDate)) {
            throw new RuntimeException("La fecha de devolución no puede ser anterior a la fecha de préstamo");
        }

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

        return "redirect:/admin/admin-panel#seccion-prestamos";
    }
}
