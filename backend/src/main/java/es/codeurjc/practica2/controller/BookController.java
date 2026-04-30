package es.codeurjc.practica2.controller;
 
import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
 
import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;
import es.codeurjc.practica2.model.GenreSection;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.LoanService;
import es.codeurjc.practica2.service.ReviewService;
import es.codeurjc.practica2.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
 
@Controller
public class BookController {
 
    @Autowired
    private BookService bookService;
 
    @Autowired
    private LoanService loanService;
 
    @Autowired
    private ReviewService reviewService;
 
    @Autowired
    private UserService userService;
 
    @GetMapping("/")
    public String showIndex(Model model, HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return "redirect:/base";
        }
        model.addAttribute("featuredBooks", bookService.findTopRatedBooks());
        return "index";
    }
 
    @GetMapping("/books")
    public String showBooks(
            Model model,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String availability) {
 
        List<GenreSection> sections = bookService.getBookSections(q, genre, availability, loanService);
 
        int total = sections.stream().mapToInt(s -> s.getBooks().size()).sum();
 
        String selectedAvailability = availability == null ? "" : availability.trim();
 
        model.addAttribute("genreSections", sections);
        model.addAttribute("genres", Genre.values());
        model.addAttribute("booksCount", total);
        model.addAttribute("search", q == null ? "" : q);
        model.addAttribute("selectedGenre", genre == null ? "" : genre);
        model.addAttribute("selectedAvailability", selectedAvailability);
 
        model.addAttribute("isAllGenres", genre == null || genre.isBlank());
        model.addAttribute("isGenreFiccion", "FICCION".equals(genre));
        model.addAttribute("isGenreFantasia", "FANTASIA".equals(genre));
        model.addAttribute("isGenreHistoria", "HISTORIA".equals(genre));
        model.addAttribute("isGenreCiencia", "CIENCIA".equals(genre));
        model.addAttribute("isGenreInfantil", "INFANTIL".equals(genre));
        model.addAttribute("isGenreMisterio", "MISTERIO".equals(genre));
        model.addAttribute("isGenreRomance", "ROMANCE".equals(genre));
        model.addAttribute("isGenreBiografia", "BIOGRAFIA".equals(genre));
        model.addAttribute("isGenreClasicos", "CLASICOS".equals(genre));
 
        model.addAttribute("isAllAvailability", selectedAvailability.isBlank());
        model.addAttribute("isAvailableSelected", "available".equals(selectedAvailability));
        model.addAttribute("isLoanedSelected", "loaned".equals(selectedAvailability));
 
        return "books";
    }
 
    @GetMapping("/book-detail/{id}")
    public String showBookDetail(@PathVariable Long id, Model model, HttpServletRequest request,
            @RequestParam(required = false) String deleteError) {
 
        Book book = bookService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
 
        float roundedRating = Math.round(book.getRating() * 10f) / 10f;
 
        model.addAttribute("title", book.getTitle());
        model.addAttribute("author", book.getAuthor());
        model.addAttribute("year", book.getYear());
        model.addAttribute("genre", book.getGenreDisplayName());
        model.addAttribute("isbn", book.getIsbn());
        model.addAttribute("description", book.getDescription());
        model.addAttribute("book_rating", roundedRating);
        model.addAttribute("bookId", book.getId());
        model.addAttribute("image", book.getImage());
        model.addAttribute("stars", bookService.buildStarList(book.getRating()));
 
        boolean isAdmin = request.isUserInRole("ADMIN");
        Long currentUserId = null;
 
        if (request.getUserPrincipal() != null) {
            String email = request.getUserPrincipal().getName();
            User currentUser = userService.findByEmail(email).orElse(null);
            if (currentUser != null) {
                currentUserId = currentUser.getId();
            }
        }
 
        final Long finalUserId = currentUserId;
        List<ReviewViewModel> reviewVMs = book.getReviews().stream()
                .map(r -> new ReviewViewModel(r, isAdmin || r.getUser().getId().equals(finalUserId)))
                .toList();
        model.addAttribute("reviews", reviewVMs);
 
        if (request.getUserPrincipal() != null) {
            String email = request.getUserPrincipal().getName();
            userService.findByEmail(email).ifPresent(user ->
                    reviewService.findByUserAndBook(user, book).ifPresentOrElse(
                            review -> model.addAttribute("userReviewId", review.getId()),
                            () -> model.addAttribute("canReview", true)
                    )
            );
        }
 
        if ("true".equals(deleteError)) {
            model.addAttribute("deleteError",
                    "No se puede eliminar este libro porque tiene préstamos activos o vencidos. "
                            + "Primero debes resolver todos los préstamos relacionados.");
        }
 
        model.addAttribute("isAvailable", loanService.isBookAvailable(book));
        model.addAttribute("logged", request.getUserPrincipal() != null);
        model.addAttribute("admin", isAdmin);
 
        return "book-detail";
    }
 
    @DeleteMapping("/book/{id}")
    public String deleteBook(@PathVariable Long id) {
        boolean deleted = loanService.deleteBookIfAllowed(id);
        if (!deleted) {
            return "redirect:/book-detail/" + id + "?deleteError=true";
        }
        return "redirect:/admin/admin-panel";
    }
 
    @PostMapping("/book-detail/{id}/loan")
    public String requestLoan(@PathVariable Long id, HttpServletRequest request, Model model) {
        String email = request.getUserPrincipal().getName();
 
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        Book book = bookService.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
 
        if (loanService.hasActiveOrOverdueLoan(user, book)) {
            float roundedRating = Math.round(book.getRating() * 10f) / 10f;
            model.addAttribute("title", book.getTitle());
            model.addAttribute("author", book.getAuthor());
            model.addAttribute("year", book.getYear());
            model.addAttribute("genre", book.getGenreDisplayName());
            model.addAttribute("isbn", book.getIsbn());
            model.addAttribute("description", book.getDescription());
            model.addAttribute("book_rating", roundedRating);
            model.addAttribute("bookId", book.getId());
            model.addAttribute("reviews", book.getReviews());
            model.addAttribute("stars", bookService.buildStarList(book.getRating()));
            model.addAttribute("loanError", "Ya tienes este libro prestado o pendiente de devolución.");
            model.addAttribute("isAvailable", loanService.isBookAvailable(book));
            return "book-detail";
        }
 
        if (!loanService.isBookAvailable(book)) {
            return "redirect:/book-detail/" + id;
        }
 
        loanService.createLoan(user, book);
        return "redirect:/my-loans";
    }
}