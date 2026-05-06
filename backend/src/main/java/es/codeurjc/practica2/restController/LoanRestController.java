package es.codeurjc.practica2.restController;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.practica2.dto.DtoMapper;
import es.codeurjc.practica2.dto.LoanDTO;
import es.codeurjc.practica2.dto.LoanRequestDTO;
import es.codeurjc.practica2.dto.LoanUpdateDTO;
import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.LoanService;
import es.codeurjc.practica2.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanRestController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    // -------------------------------------------------------
    // GET /api/v1/loans (ADMIN)
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<Page<LoanDTO>> getLoans(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Loan.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            HttpServletRequest request) {

        if (!request.isUserInRole("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo los administradores pueden acceder a esta información");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Loan> loanPage = loanService.searchLoans(q, status, pageable);

        return ResponseEntity.ok(loanPage.map(DtoMapper::toLoanDTO));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<LoanDTO>> getMyLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);

        Pageable pageable = PageRequest.of(page, size);

        Page<Loan> loanPage = loanService.findByUser(currentUser, pageable);

        return ResponseEntity.ok(loanPage.map(DtoMapper::toLoanDTO));
    }

    // -------------------------------------------------------
    // GET /api/v1/loans/{id}
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<LoanDTO> getLoan(
            @PathVariable Long id,
            HttpServletRequest request) {

        Loan loan = findLoanOrThrow(id);
        User currentUser = getCurrentUser(request);

        boolean isAdmin = request.isUserInRole("ADMIN");
        boolean isOwner = loan.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a este préstamo");
        }

        return ResponseEntity.ok(DtoMapper.toLoanDTO(loan));
    }

    // -------------------------------------------------------
    // POST /api/v1/loans
    // -------------------------------------------------------
    @PostMapping
    public ResponseEntity<LoanDTO> createLoan(
            @RequestBody LoanRequestDTO dto,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);

        if (dto.bookId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookId is required");
        }

        Book book = bookService.findById(dto.bookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        if (!loanService.isBookAvailable(book)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El libro no está disponible");
        }

        if (loanService.hasActiveOrOverdueLoan(currentUser, book)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya tienes este libro en préstamo o pendiente de devolución");
        }

        Loan loan = loanService.createLoan(currentUser, book);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(loan.getId())
                .toUri();

        return ResponseEntity.created(location).body(DtoMapper.toLoanDTO(loan));
    }

    // -------------------------------------------------------
    // PUT /api/v1/loans/{id}/return
    // -------------------------------------------------------
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanDTO> returnLoan(
            @PathVariable Long id,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);

        if (request.isUserInRole("ADMIN")) {
            loanService.markAsReturned(id);
        } else {
            loanService.returnLoanByUser(id, currentUser);
        }

        Loan updatedLoan = findLoanOrThrow(id);

        return ResponseEntity.ok(DtoMapper.toLoanDTO(updatedLoan));
    }

    // -------------------------------------------------------
    // PUT /api/v1/loans/{id}
    // -------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<LoanDTO> updateLoan(
            @PathVariable Long id,
            @RequestBody LoanUpdateDTO dto,
            HttpServletRequest request) {

        if (!request.isUserInRole("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo los administradores pueden actualizar préstamos");
        }

        loanService.validateAndUpdateLoan(
                id,
                dto.loanDate(),
                dto.returnDate(),
                dto.status());

        Loan updatedLoan = findLoanOrThrow(id);

        return ResponseEntity.ok(DtoMapper.toLoanDTO(updatedLoan));
    }

    // -------------------------------------------------------
    // DELETE /api/v1/loans/{id}
    // -------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(
            @PathVariable Long id,
            HttpServletRequest request) {

        if (!request.isUserInRole("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo los administradores pueden eliminar préstamos");
        }

        Loan loan = findLoanOrThrow(id);

        if (!loan.isReturned()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo los préstamos devueltos pueden ser eliminados");
        }

        loanService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------
    private User getCurrentUser(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();

        return userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private Loan findLoanOrThrow(Long id) {
        return loanService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Préstamo no encontrado"));
    }
}