package es.codeurjc.practica2.restController;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    // GET /api/v1/loans
    // Admin: list all loans, with optional filters
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<LoanDTO>> getLoans(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        if (!request.isUserInRole("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los administradores pueden acceder a esta información");
        }

        List<LoanDTO> loans = loanService.searchLoans(q, status).stream()
                .map(DtoMapper::toLoanDTO)
                .toList();

        return ResponseEntity.ok(loans);
    }

    // -------------------------------------------------------
    // GET /api/v1/loans/me
    // User: list own loans
    // -------------------------------------------------------
    @GetMapping("/me")
    public ResponseEntity<List<LoanDTO>> getMyLoans(HttpServletRequest request) {

        User currentUser = getCurrentUser(request);

        List<LoanDTO> loans = loanService.getLoansForUser(currentUser).stream()
                .map(DtoMapper::toLoanDTO)
                .toList();

        return ResponseEntity.ok(loans);
    }

    // -------------------------------------------------------
    // GET /api/v1/loans/{id}
    // Admin or owner: get one loan
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
    // User: create a loan for the authenticated user
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
    // User owner or admin: mark loan as returned
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
    // Admin: update loan dates and status
    // -------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<LoanDTO> updateLoan(
            @PathVariable Long id,
            @RequestBody LoanUpdateDTO dto,
            HttpServletRequest request) {

        if (!request.isUserInRole("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los administradores pueden actualizar préstamos");
        }

        List<String> errors = loanService.validateAndUpdateLoan(
                id,
                dto.loanDate(),
                dto.returnDate(),
                dto.status());

        if (!errors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join(" ", errors));
        }

        Loan updatedLoan = findLoanOrThrow(id);
        return ResponseEntity.ok(DtoMapper.toLoanDTO(updatedLoan));
    }

    // -------------------------------------------------------
    // DELETE /api/v1/loans/{id}
    // Admin: delete returned loans from history
    // -------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(
            @PathVariable Long id,
            HttpServletRequest request) {

        if (!request.isUserInRole("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los administradores pueden eliminar préstamos");
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
