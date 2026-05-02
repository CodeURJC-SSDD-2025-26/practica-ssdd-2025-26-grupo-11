package es.codeurjc.practica2.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.LoanService;
import es.codeurjc.practica2.service.ReviewService;
import es.codeurjc.practica2.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private BookService bookService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    // -------------------------------------------------------------------------
    // Admin panel
    // -------------------------------------------------------------------------

    @GetMapping("/admin-panel")
    public String showAdminPanel(
            Model model,
            @RequestParam(required = false) String bookQ,
            @RequestParam(required = false) String bookGenre,
            @RequestParam(required = false) String loanQ,
            @RequestParam(required = false) String loanStatus,
            @RequestParam(required = false) String reviewQ,
            @RequestParam(required = false) String userQ) {

        // --- Books (reuse existing searchBooks from BookService) ---
        Genre genreEnum = (bookGenre == null || bookGenre.isBlank()) ? null
                : Genre.valueOf(bookGenre.toUpperCase());
        String bookParam = (bookQ == null || bookQ.isBlank()) ? null : bookQ.trim().toLowerCase();
        List<Book> books = bookService.searchBooks(bookParam, genreEnum);
        for (Book book : books) {
            book.setAvailable(loanService.isBookAvailable(book));
        }

        // --- Loans ---
        List<Loan> loans = loanService.searchLoans(loanQ, loanStatus);

        // --- Reviews ---
        List<Review> reviews = reviewService.searchReviews(reviewQ);

        // --- Users ---
        List<User> users = userService.searchUsers(userQ);

        // --- Charts (always use full data, not filtered) ---
        List<String> genreLabels = new ArrayList<>();
        List<Integer> genreLoanCounts = new ArrayList<>();
        List<String> genreRatingLabels = new ArrayList<>();
        List<Double> genreRatingValues = new ArrayList<>();

        Map<Genre, Long> loanCountMap = new HashMap<>();
        for (Object[] row : loanService.countLoansByGenre()) {
            loanCountMap.put((Genre) row[0], (Long) row[1]);
        }

        Map<Genre, Double> ratingMap = new HashMap<>();
        for (Object[] row : bookService.avgRatingByGenre()) {
            ratingMap.put((Genre) row[0], (Double) row[1]);
        }

        for (Genre genre : Genre.values()) {
            String name = genre.getDisplayName();
            long count = loanCountMap.getOrDefault(genre, 0L);
            double avg = Math.round(ratingMap.getOrDefault(genre, 0.0) * 10.0) / 10.0;

            genreLabels.add(name);
            genreLoanCounts.add((int) count);
            genreRatingLabels.add(name);
            genreRatingValues.add(avg);
        }

        model.addAttribute("books", books);
        model.addAttribute("loans", loans);
        model.addAttribute("reviews", reviews);
        model.addAttribute("users", users);

        // Stats (always from full DB, not affected by filters)
        model.addAttribute("booksCount", bookService.findAll().size());
        model.addAttribute("usersCount", userService.findAll().size());
        model.addAttribute("reviewsCount", reviewService.findAll().size());
        model.addAttribute("activeLoansCount", loanService.countByStatus(Loan.Status.ACTIVO));
        model.addAttribute("overdueLoansCount", loanService.countByStatus(Loan.Status.VENCIDO));
        model.addAttribute("returnedLoansCount", loanService.countByStatus(Loan.Status.DEVUELTO));

        // Current filter values (to keep inputs populated after submit)
        model.addAttribute("bookQ", bookQ == null ? "" : bookQ);
        model.addAttribute("bookGenre", bookGenre == null ? "" : bookGenre);
        model.addAttribute("loanQ", loanQ == null ? "" : loanQ);
        model.addAttribute("loanStatus", loanStatus == null ? "" : loanStatus);
        model.addAttribute("reviewQ", reviewQ == null ? "" : reviewQ);
        model.addAttribute("userQ", userQ == null ? "" : userQ);

        // Book genre selector
        model.addAttribute("isBookGenreFiccion",   "FICCION".equals(bookGenre));
        model.addAttribute("isBookGenreFantasia",  "FANTASIA".equals(bookGenre));
        model.addAttribute("isBookGenreHistoria",  "HISTORIA".equals(bookGenre));
        model.addAttribute("isBookGenreCiencia",   "CIENCIA".equals(bookGenre));
        model.addAttribute("isBookGenreInfantil",  "INFANTIL".equals(bookGenre));
        model.addAttribute("isBookGenreMisterio",  "MISTERIO".equals(bookGenre));
        model.addAttribute("isBookGenreRomance",   "ROMANCE".equals(bookGenre));
        model.addAttribute("isBookGenreBiografia", "BIOGRAFIA".equals(bookGenre));
        model.addAttribute("isBookGenreClasicos",  "CLASICOS".equals(bookGenre));

        // Loan status filter buttons
        model.addAttribute("isLoanStatusActivo",   "ACTIVO".equals(loanStatus));
        model.addAttribute("isLoanStatusVencido",  "VENCIDO".equals(loanStatus));
        model.addAttribute("isLoanStatusDevuelto", "DEVUELTO".equals(loanStatus));

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            model.addAttribute("genreLabelsJson", objectMapper.writeValueAsString(genreLabels));
            model.addAttribute("genreLoanCountsJson", objectMapper.writeValueAsString(genreLoanCounts));
            model.addAttribute("genreRatingLabelsJson", objectMapper.writeValueAsString(genreRatingLabels));
            model.addAttribute("genreRatingValuesJson", objectMapper.writeValueAsString(genreRatingValues));
        } catch (Exception e) {
            throw new RuntimeException("Error generating chart data", e);
        }

        return "admin/admin-panel";
    }

    // -------------------------------------------------------------------------
    // Add book
    // -------------------------------------------------------------------------

    @GetMapping("/admin-add-book")
    public String showAdminAddBook() {
        return "admin/admin-add-book";
    }

    @PostMapping("/admin-add-book")
    public String newBookProcess(
            Model model,
            Book book,
            @RequestParam(name = "genre") String genreString,
            @RequestParam MultipartFile imageField,
            @RequestParam(name = "year", required = false) String yearStr,
            @RequestParam(name = "isbn", required = false) String isbnStr) throws IOException {

        List<String> errors = bookService.createBook(book, genreString, imageField, yearStr, isbnStr);

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return "admin/admin-add-book";
        }

        return "redirect:/book-detail/" + book.getId();
    }

    // -------------------------------------------------------------------------
    // Edit book
    // -------------------------------------------------------------------------

    @GetMapping("/admin-edit-book/{id}")
    public String showAdminEditBook(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        model.addAttribute("book", book);
        model.addAttribute("currentGenre", book.getGenre() != null ? book.getGenre().name() : "");
        return "admin/admin-edit-book";
    }

    @PostMapping("/admin-edit-book/{id}")
    public String editBookProcess(
            @PathVariable Long id,
            Book updatedBook,
            @RequestParam(name = "genre") String genreString,
            @RequestParam(required = false, defaultValue = "false") boolean removeImage,
            @RequestParam(required = false) MultipartFile imageField,
            @RequestParam(name = "year", required = false) String yearStr,
            @RequestParam(name = "isbn", required = false) String isbnStr,
            Model model) throws IOException, SQLException {

        List<String> errors = bookService.updateBook(id, updatedBook, genreString,
                removeImage, imageField, yearStr, isbnStr);

        if (!errors.isEmpty()) {
            Book dbBook = bookService.findById(id).orElseThrow();
            model.addAttribute("errors", errors);
            model.addAttribute("book", dbBook);
            model.addAttribute("currentGenre", genreString);
            return "admin/admin-edit-book";
        }

        return "redirect:/book-detail/" + id;
    }

    // -------------------------------------------------------------------------
    // Loan management
    // -------------------------------------------------------------------------

    @PostMapping("/loan/{id}/return")
    public String markLoanAsReturned(@PathVariable Long id) {
        loanService.markAsReturned(id);
        return "redirect:/admin/admin-panel";
    }

    @GetMapping("/edit-loans/{id}")
    public String editLoanForm(@PathVariable Long id, Model model) {
        Loan loan = loanService.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        model.addAttribute("loan", loan);
        model.addAttribute("statuses", Loan.Status.values());
        return "admin/admin-edit-loans";
    }

    @PostMapping("/edit-loans/{id}")
    public String updateLoan(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam("loanDate") LocalDate loanDate,
            @RequestParam("returnDate") LocalDate returnDate,
            Model model) {

        List<String> errors = loanService.validateAndUpdateLoan(id, loanDate, returnDate, status);

        if (!errors.isEmpty()) {
            Loan loan = loanService.findById(id).orElseThrow();
            model.addAttribute("errors", errors);
            model.addAttribute("loan", loan);
            model.addAttribute("statuses", Loan.Status.values());
            return "admin/admin-edit-loans";
        }

        return "redirect:/admin/admin-panel#seccion-prestamos";
    }

    @PostMapping("/loan/{id}/delete")
    public String deleteLoanAsAdmin(@PathVariable Long id) {
        loanService.deleteById(id);
        return "redirect:/admin/admin-panel#seccion-prestamos";
    }

    // -------------------------------------------------------------------------
    // Review management
    // -------------------------------------------------------------------------

    @PostMapping("/review/{id}/delete")
    public String deleteReviewAsAdmin(@PathVariable Long id) {
        reviewService.deleteById(id);
        return "redirect:/admin/admin-panel#seccion-resenas";
    }

    // -------------------------------------------------------------------------
    // User management
    // -------------------------------------------------------------------------

    @GetMapping("/admin/user/{id}")
    public String viewUserFromAdmin(@PathVariable Long id) {
        return "redirect:/user/" + id;
    }

    @PostMapping("/user/{id}/delete")
    public String deleteUserAsAdmin(@PathVariable Long id, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        User userToDelete = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String loggedInEmail = request.getUserPrincipal().getName();
        if (userToDelete.getEmail().equals(loggedInEmail)) {
            redirectAttributes.addFlashAttribute("error", "No puedes eliminar tu propia cuenta");
            return "redirect:/admin/admin-panel#seccion-usuarios";
        }

        userService.deleteUser(id, reviewService);
        return "redirect:/admin/admin-panel#seccion-usuarios";
    }
}