package es.codeurjc.practica2.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;
import es.codeurjc.practica2.model.Image;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.LoanRepository;
import es.codeurjc.practica2.repository.ReviewRepository;
import es.codeurjc.practica2.repository.UserRepository;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.ImageService;
import es.codeurjc.practica2.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import es.codeurjc.practica2.model.Review;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    // Get first 4 most rated books
    @GetMapping("/")
    public String showIndex(Model model, HttpServletRequest request) {

        // Si hay usuario logueado, redirige a /base
        if (request.getUserPrincipal() != null) {
            return "redirect:/base";
        }

        model.addAttribute("featuredBooks", bookService.findTopRatedBooks());
        return "index";
    }

    @GetMapping("/books")
    public String showBooks(Model model) {
        List<Book> books = bookService.findAll();
        model.addAttribute("books", books);
        return "books";
    }

    @GetMapping("/book-detail/{id}")
    public String showBookDetail(@PathVariable Long id, Model model, HttpServletRequest request) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado con id " + id));

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
        Long currentUserId = null;
        boolean isAdmin = request.isUserInRole("ADMIN");
        if (request.getUserPrincipal() != null) {
            String email = request.getUserPrincipal().getName();
            User currentUser = userRepository.findByEmail(email).orElse(null);
            if (currentUser != null)
                currentUserId = currentUser.getId();
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

        return "book-detail";
    }

    @PostMapping("/admin/admin-add-book")
    public String newBookProcess(Model model,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String description,
            @RequestParam(name = "genre") String genreString,
            @RequestParam long isbn,
            @RequestParam int year,
            MultipartFile imageField) throws IOException {

        try {
            Genre genre = Genre.valueOf(genreString.toUpperCase());
            Book book = new Book(title, author, description, genre, 0f, year, isbn);

            if (!imageField.isEmpty()) {
                Image image = imageService.createImage(imageField.getInputStream());
                book.setImage(image);
            }

            bookService.save(book);

            model.addAttribute("id", book.getId());
            model.addAttribute("date", year);

            return "redirect:/book-detail/" + book.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Género inválido");
            return "admin/admin-add-book";
        }
    }

    // DELETE BOOK
    @DeleteMapping("/book/{id}")
    public String deleteBook(@PathVariable Long id) {
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
            return "book-detail";
        }

        loanService.createLoan(user, book);

        return "redirect:/my-loans";
    }

    private void updateImage(Book book, boolean removeImage, MultipartFile imageField)
            throws IOException, SQLException {

        if (!imageField.isEmpty()) {
            Book dbBook = bookService.findById(book.getId()).orElseThrow();

            if (dbBook.getImage() == null) {
                Image image = imageService.createImage(imageField.getInputStream());
                book.setImage(image);
            } else {
                Image image = imageService.replaceImageFile(dbBook.getImage().getId(), imageField.getInputStream());
                book.setImage(image);
            }
        } else {
            if (removeImage) {
                if (book.getImage() != null) {
                    imageService.deleteImage(book.getImage().getId());
                    book.setImage(null);
                }
            } else {
                // Maintain the same image loading it before updating the book
                Book dbBook = bookService.findById(book.getId()).orElseThrow();
                book.setImage(dbBook.getImage());
            }
        }
    }

    @GetMapping("/admin/admin-panel")
    public String showAdminPanel(Model model) {
        List<Book> books = bookService.findAll();
        List<Loan> loans = loanRepository.findAll();
        List<Review> reviews = reviewRepository.findAll();

        long activeLoansCount = loans.stream()
                .filter(loan -> loan.getStatus() == Loan.Status.ACTIVO)
                .count();

        long overdueLoansCount = loans.stream()
                .filter(loan -> loan.getStatus() == Loan.Status.VENCIDO)
                .count();

        long returnedLoansCount = loans.stream()
                .filter(loan -> loan.getStatus() == Loan.Status.DEVUELTO)
                .count();

        model.addAttribute("books", books);
        model.addAttribute("loans", loans);
        model.addAttribute("reviews", reviews);

        model.addAttribute("booksCount", books.size());
        model.addAttribute("usersCount", userRepository.count());
        model.addAttribute("reviewsCount", reviews.size());

        model.addAttribute("activeLoansCount", activeLoansCount);
        model.addAttribute("overdueLoansCount", overdueLoansCount);
        model.addAttribute("returnedLoansCount", returnedLoansCount);

        return "admin/admin-panel";
    }

    @GetMapping("/admin/admin-add-book")
    public String showAdminAddBook() {
        return "admin/admin-add-book";
    }

    @GetMapping("/admin/admin-edit-book/{id}")
    public String showAdminEditBook(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        model.addAttribute("book", book);
        model.addAttribute("currentGenre", book.getGenre() != null ? book.getGenre().name() : "");
        return "admin/admin-edit-book";
    }

    @PostMapping("/admin/admin-edit-book/{id}")
    public String editBookProcess(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String description,
            @RequestParam(name = "genre") String genreString,
            @RequestParam long isbn,
            @RequestParam int year,
            @RequestParam(required = false, defaultValue = "false") boolean removeImage,
            @RequestParam(required = false) MultipartFile imageField) throws IOException, SQLException {

        Book dbBook = bookService.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        try {
            Genre genre = Genre.valueOf(genreString.toUpperCase());

            dbBook.setTitle(title);
            dbBook.setAuthor(author);
            dbBook.setYear(year);
            dbBook.setGenre(genre);
            dbBook.setIsbn(isbn);
            dbBook.setDescription(description);

            updateImage(dbBook, removeImage, imageField);

            bookService.save(dbBook);

            return "redirect:/admin/admin-panel";
        } catch (IllegalArgumentException e) {
            // Si hay error, recargar la página de edición con error
            return "redirect:/admin/admin-edit-book/" + id;
        }
    }

    @PostMapping("/admin/loan/{id}/return")
    public String markLoanAsReturned(@PathVariable Long id) {
        loanService.markAsReturned(id);
        return "redirect:/admin/admin-panel";
    }

    @PostMapping("/admin/review/{id}/delete")
    public String deleteReviewAsAdmin(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/admin/admin-panel#seccion-resenas";
    }
}
