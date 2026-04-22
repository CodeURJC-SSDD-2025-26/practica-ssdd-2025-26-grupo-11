package es.codeurjc.practica2.controller;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import es.codeurjc.practica2.repository.LoanRepository;
import es.codeurjc.practica2.repository.ReviewRepository;
import es.codeurjc.practica2.repository.UserRepository;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.LoanService;
import es.codeurjc.practica2.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class BookController {
    private final ReviewService reviewService;

    @Autowired
    private BookService bookService;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    BookController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Get first 4 most rated books
    @GetMapping("/")
    public String showIndex(Model model, HttpServletRequest request) {

        // If there is a logged user, redirect to /base
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

        String normalizedQuery = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);
        Genre genreEnum = (genre == null || genre.isBlank()) ? null : Genre.valueOf(genre);

        // Database-level filtering (PREVIOUSLY: findAll + Java filters)
        List<Book> books = bookService.searchBooks(normalizedQuery, genreEnum);

        // We only calculate availability
        for (Book book : books) {
            book.setAvailable(loanService.isBookAvailable(book));
        }

        String selectedAvailability = availability == null ? "" : availability.trim();

        List<Book> filteredBooks = books.stream()
                .filter(book -> {
                    if (selectedAvailability.isBlank())
                        return true;
                    if ("available".equals(selectedAvailability))
                        return book.isAvailable();
                    if ("loaned".equals(selectedAvailability))
                        return !book.isAvailable();
                    return true;
                })
                .toList();

        // Group by genre
        Map<Genre, List<Book>> groupedBooks = new LinkedHashMap<>();
        for (Genre g : Genre.values()) {
            groupedBooks.put(g, new ArrayList<>());
        }

        for (Book book : filteredBooks) {
            if (book.getGenre() != null) {
                groupedBooks.get(book.getGenre()).add(book);
            }
        }

        List<GenreSection> sections = new ArrayList<>();
        for (Genre g : Genre.values()) {
            List<Book> booksOfGenre = groupedBooks.get(g);
            if (booksOfGenre != null && !booksOfGenre.isEmpty()) {
                sections.add(new GenreSection(g.getDisplayName(), g.name(), booksOfGenre));
            }
        }

        model.addAttribute("genreSections", sections);
        model.addAttribute("genres", Genre.values());
        model.addAttribute("booksCount", filteredBooks.size());

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));

        model.addAttribute("title", book.getTitle());
        model.addAttribute("author", book.getAuthor());
        model.addAttribute("year", book.getYear());
        model.addAttribute("genre", book.getGenreDisplayName());
        model.addAttribute("isbn", book.getIsbn());
        model.addAttribute("description", book.getDescription());
        float roundedRating = Math.round(book.getRating() * 10f) / 10f;
        model.addAttribute("book_rating", roundedRating);
        model.addAttribute("bookId", book.getId());
        model.addAttribute("image", book.getImage());
        model.addAttribute("reviews", book.getReviews());

        List<String> stars = new ArrayList<>();
        float rating = book.getRating();
        for (int i = 1; i <= 5; i++) {
            if (rating >= i) {
                stars.add("fa-solid fa-star");
            } else if (rating >= i - 0.5) {
                stars.add("fa-solid fa-star-half-stroke");
            } else {
                stars.add("fa-regular fa-star");
            }
        }
        model.addAttribute("stars", stars);
        Long currentUserId = null;
        boolean isAdmin = request.isUserInRole("ADMIN");
        if (request.getUserPrincipal() != null) {
            String email = request.getUserPrincipal().getName();
            User currentUser = userRepository.findByEmail(email).orElse(null);
            if (currentUser != null) {
                currentUserId = currentUser.getId();
            }
        }

        final Long finalUserId = currentUserId;
        List<ReviewViewModel> reviewVMs = book.getReviews().stream()
                .map(r -> new ReviewViewModel(r, isAdmin || r.getUser().getId().equals(finalUserId)))
                .toList();

        model.addAttribute("reviews", reviewVMs);

        // Logged user can only review if they haven't already reviewed this book
        if (request.getUserPrincipal() != null) {
            String email = request.getUserPrincipal().getName();
            userRepository.findByEmail(email).ifPresent(user -> {
                reviewRepository.findByUserAndBook(user, book).ifPresentOrElse(
                        review -> {
                            model.addAttribute("userReviewId", review.getId());
                        },
                        () -> {
                            model.addAttribute("canReview", true);
                        });
            });
        }

        // Handle delete error if present
        if (deleteError != null && deleteError.equals("true")) {
            model.addAttribute("deleteError",
                    "No se puede eliminar este libro porque tiene préstamos activos o vencidos. Primero debes resolver todos los préstamos relacionados.");
        }

        // Check book availability
        boolean isAvailable = loanService.isBookAvailable(book);
        model.addAttribute("isAvailable", isAvailable);

        model.addAttribute("logged", request.getUserPrincipal() != null);
        model.addAttribute("admin", isAdmin);

        return "book-detail";
    }


    // DELETE BOOK
    @DeleteMapping("/book/{id}")
    public String deleteBook(@PathVariable Long id, Model model, HttpServletRequest request) {
        // Check if book has active or overdue loans
        if (loanRepository.countActiveOrOverdueLoans(id) > 0) {
            return "redirect:/book-detail/" + id + "?deleteError=true";
        }

        // Delete all returned loans for this book
        loanRepository.deleteReturnedLoansByBook(id);

        // Delete the book
        bookService.deleteById(id);
        return "redirect:/admin/admin-panel";
    }

    @PostMapping("/book-detail/{id}/loan")
    public String requestLoan(@PathVariable Long id, HttpServletRequest request, Model model) {
        String email = request.getUserPrincipal().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Book book = bookService.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        // Check if user already has this book rented
        if (loanService.hasActiveOrOverdueLoan(user, book)) {
            model.addAttribute("title", book.getTitle());
            model.addAttribute("author", book.getAuthor());
            model.addAttribute("year", book.getYear());
            model.addAttribute("genre", book.getGenreDisplayName());
            model.addAttribute("isbn", book.getIsbn());
            model.addAttribute("description", book.getDescription());

            float roundedRating = Math.round(book.getRating() * 10f) / 10f;
            model.addAttribute("book_rating", roundedRating);
            model.addAttribute("bookId", book.getId());
            model.addAttribute("reviews", book.getReviews());

            List<String> stars = new ArrayList<>();
            float rating = book.getRating();
            for (int i = 1; i <= 5; i++) {
                if (rating >= i) {
                    stars.add("fa-solid fa-star");
                } else if (rating >= i - 0.5) {
                    stars.add("fa-solid fa-star-half-stroke");
                } else {
                    stars.add("fa-regular fa-star");
                }
            }
            model.addAttribute("stars", stars);

            model.addAttribute("loanError", "Ya tienes este libro prestado o pendiente de devolución.");
            model.addAttribute("isAvailable", loanService.isBookAvailable(book));
            return "book-detail";
        }

        // Check if book is available
        if (!loanService.isBookAvailable(book)) {
            return "redirect:/book-detail/" + id;
        }

        loanService.createLoan(user, book);

        return "redirect:/my-loans";
    }

}
