package es.codeurjc.practica2.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;
import es.codeurjc.practica2.model.Image;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.repository.ImageRepository;
import es.codeurjc.practica2.repository.LoanRepository;
import es.codeurjc.practica2.repository.ReviewRepository;
import es.codeurjc.practica2.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import tools.jackson.databind.ObjectMapper;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.service.ReviewService;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.LoanService;
import es.codeurjc.practica2.service.ImageService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookService bookService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private LoanService loanService;

    @PostMapping("/admin-add-book")
    public String newBookProcess(
            Model model,
            Book book,
            @RequestParam(name = "genre") String genreString,
            @RequestParam MultipartFile imageField,
            @RequestParam(name = "year", required = false) String yearStr,
            @RequestParam(name = "isbn", required = false) String isbnStr) throws IOException {

        List<String> errors = new ArrayList<>();

        if (book.getTitle() == null || book.getTitle().isBlank()) {
            errors.add("El título es obligatorio.");
        } else if (book.getTitle().length() > 20) {
            errors.add("El título no puede superar los 20 caracteres.");
        }

        if (book.getAuthor() == null || book.getAuthor().isBlank()) {
            errors.add("El autor es obligatorio.");
        } else if (book.getAuthor().length() > 25) {
            errors.add("El autor no puede superar los 25 caracteres.");
        }

        if (yearStr == null || yearStr.isBlank()) {
            errors.add("El año de publicación es obligatorio.");
        } else {
            try {
                int year = Integer.parseInt(yearStr);
                if (year < 1000 || year > 2099) {
                    errors.add("El año debe estar entre 1000 y 2099.");
                }
            } catch (NumberFormatException e) {
                errors.add("El año debe ser un número válido.");
            }
        }

        if (isbnStr == null || isbnStr.isBlank()) {
            errors.add("El ISBN es obligatorio.");
        } else {
            try {
                Long.parseLong(isbnStr); 
            } catch (NumberFormatException e) {
                errors.add("El ISBN debe ser un número válido.");
            }
        }

        if (book.getDescription() != null && book.getDescription().length() > 150) {
            errors.add("La descripción no puede superar los 150 caracteres.");
        }

        Genre genre = null;
        try {
            if (genreString == null || genreString.isBlank()) {
                errors.add("El género es obligatorio.");
            } else {
                genre = Genre.valueOf(genreString.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            errors.add("El género seleccionado no es válido.");
        }

        if (!imageField.isEmpty()) {
            String contentType = imageField.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                errors.add("La imagen debe ser .jpg o .png.");
            }
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return "admin/admin-add-book";
        }

        book.setGenre(genre);

        if (!imageField.isEmpty()) {
            Image image = imageService.createImage(imageField.getInputStream());
            book.setImage(image);
        } else {
            Resource defaultImage = new ClassPathResource("static/images/default-book.png");
            Image image = imageService.createImage(defaultImage.getInputStream());
            book.setImage(image);
        }

        bookService.save(book);
        return "redirect:/book-detail/" + book.getId();
    }

    @GetMapping("/admin-panel")
    public String showAdminPanel(Model model) {

        List<Book> books = bookService.findAll();
        List<Loan> loans = loanRepository.findAll();
        List<Review> reviews = reviewRepository.findAll();
        List<User> users = userRepository.findAll();

        for (Book book : books) {
            book.setAvailable(loanService.isBookAvailable(book));
        }

        for (Loan loan : loans) {
            if (loan.getStatus() != Loan.Status.DEVUELTO) {
                loan.refreshStatusFromDates();
            }
        }

        List<String> genreLabels = new ArrayList<>();
        List<Integer> genreLoanCounts = new ArrayList<>();
        List<String> genreRatingLabels = new ArrayList<>();
        List<Double> genreRatingValues = new ArrayList<>();

        Map<Genre, Long> loanCountMap = new HashMap<>();
        for (Object[] row : loanRepository.countLoansByGenre()) {
            loanCountMap.put((Genre) row[0], (Long) row[1]);
        }

        Map<Genre, Double> ratingMap = new HashMap<>();
        for (Object[] row : bookService.avgRatingByGenre()) {
            ratingMap.put((Genre) row[0], (Double) row[1]);
        }

        for (Genre genre : Genre.values()) {
            String genreName = genre.getDisplayName();

            long loanCount = loanCountMap.getOrDefault(genre, 0L);
            double avgRating = ratingMap.getOrDefault(genre, 0.0);

            avgRating = Math.round(avgRating * 10.0) / 10.0;

            genreLabels.add(genreName);
            genreLoanCounts.add((int) loanCount);
            genreRatingLabels.add(genreName);
            genreRatingValues.add(avgRating);
        }

        long activeLoansCount = loanRepository.countByStatus(Loan.Status.ACTIVO);
        long overdueLoansCount = loanRepository.countByStatus(Loan.Status.VENCIDO);
        long returnedLoansCount = loanRepository.countByStatus(Loan.Status.DEVUELTO);

        model.addAttribute("books", books);
        model.addAttribute("loans", loans);
        model.addAttribute("reviews", reviews);
        model.addAttribute("users", users);

        model.addAttribute("booksCount", books.size());
        model.addAttribute("usersCount", users.size());
        model.addAttribute("reviewsCount", reviews.size());

        model.addAttribute("activeLoansCount", activeLoansCount);
        model.addAttribute("overdueLoansCount", overdueLoansCount);
        model.addAttribute("returnedLoansCount", returnedLoansCount);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            model.addAttribute("genreLabelsJson", objectMapper.writeValueAsString(genreLabels));
            model.addAttribute("genreLoanCountsJson", objectMapper.writeValueAsString(genreLoanCounts));
            model.addAttribute("genreRatingLabelsJson", objectMapper.writeValueAsString(genreRatingLabels));
            model.addAttribute("genreRatingValuesJson", objectMapper.writeValueAsString(genreRatingValues));
        } catch (Exception e) {
            throw new RuntimeException("Error generando los datos de los gráficos", e);
        }

        return "admin/admin-panel";
    }

    @GetMapping("/admin-add-book")
    public String showAdminAddBook() {
        return "admin/admin-add-book";
    }

    @GetMapping("/admin-edit-book/{id}")
    public String showAdminEditBook(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

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

        Book dbBook = bookService.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        List<String> errors = new ArrayList<>();

        if (updatedBook.getTitle() == null || updatedBook.getTitle().isBlank()) {
            errors.add("El título es obligatorio.");
        } else if (updatedBook.getTitle().length() > 20) {
            errors.add("El título no puede superar los 20 caracteres.");
        }

        if (updatedBook.getAuthor() == null || updatedBook.getAuthor().isBlank()) {
            errors.add("El autor es obligatorio.");
        } else if (updatedBook.getAuthor().length() > 25) {
            errors.add("El autor no puede superar los 25 caracteres.");
        }

        if (yearStr == null || yearStr.isBlank()) {
            errors.add("El año de publicación es obligatorio.");
        } else {
            try {
                int year = Integer.parseInt(yearStr);
                if (year < 1000 || year > 2099) {
                    errors.add("El año debe estar entre 1000 y 2099.");
                }
            } catch (NumberFormatException e) {
                errors.add("El año debe ser un número válido.");
            }
        }

        if (isbnStr == null || isbnStr.isBlank()) {
            errors.add("El ISBN es obligatorio.");
        } else {
            try {
                Long.parseLong(isbnStr);
            } catch (NumberFormatException e) {
                errors.add("El ISBN debe ser un número válido.");
            }
        }

        if (updatedBook.getDescription() != null && updatedBook.getDescription().length() > 150) {
            errors.add("La descripción no puede superar los 150 caracteres.");
        }

        Genre genre = null;
        try {
            if (genreString == null || genreString.isBlank()) {
                errors.add("El género es obligatorio.");
            } else {
                genre = Genre.valueOf(genreString.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            errors.add("El género seleccionado no es válido.");
        }

        if (imageField != null && !imageField.isEmpty()) {
            String contentType = imageField.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                errors.add("La imagen debe ser .jpg o .png.");
            }
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("book", dbBook);
            model.addAttribute("currentGenre", genreString);
            return "admin/admin-edit-book";
        }

        dbBook.setTitle(updatedBook.getTitle());
        dbBook.setAuthor(updatedBook.getAuthor());
        dbBook.setDescription(updatedBook.getDescription());
        dbBook.setYear(updatedBook.getYear());
        dbBook.setIsbn(updatedBook.getIsbn());
        dbBook.setGenre(genre);

        updateImage(dbBook, removeImage, imageField);

        bookService.save(dbBook);

        return "redirect:/book-detail/" + id;
    }

    @PostMapping("/loan/{id}/return")
    public String markLoanAsReturned(@PathVariable Long id) {
        loanService.markAsReturned(id);
        return "redirect:/admin/admin-panel";
    }

    @PostMapping("/review/{id}/delete")
    public String deleteReviewAsAdmin(@PathVariable Long id) {
        reviewService.deleteById(id);
        return "redirect:/admin/admin-panel#seccion-resenas";
    }

    @GetMapping("/admin/user/{id}")
    public String viewUserFromAdmin(@PathVariable Long id) {
        return "redirect:/user/" + id;
    }

    @PostMapping("/user/{id}/delete")
    public String deleteUserAsAdmin(@PathVariable Long id, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        User userToDelete = userRepository.findById(id).orElseThrow();

        // The admin cannot delete himself or herself
        String loggedInEmail = request.getUserPrincipal().getName();
        if (userToDelete.getEmail().equals(loggedInEmail)) {
            redirectAttributes.addFlashAttribute("error", "No puedes eliminar tu propia cuenta");
            return "redirect:/admin/admin-panel#seccion-usuarios";
        }

        // Delete all reviews from the user and update book ratings
        reviewService.deleteUserReviews(userToDelete);

        // Control the deletion of the image with the user
        if (userToDelete.getImage() != null) {
            Long imageId = userToDelete.getImage().getId();
            userToDelete.setImage(null);
            userRepository.save(userToDelete);
            imageRepository.deleteById(imageId);
        }

        userRepository.delete(userToDelete);

        return "redirect:/admin/admin-panel#seccion-usuarios";
    }

    @GetMapping("/edit-loans/{id}")
    public String editLoanForm(@PathVariable Long id, Model model) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        model.addAttribute("loan", loan);
        model.addAttribute("statuses", Loan.Status.values());

        return "admin/admin-edit-loans";
    }

    @PostMapping("/edit-loans/{id}")
    public String updateLoan(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam("loanDate") java.time.LocalDate loanDate,
            @RequestParam("returnDate") java.time.LocalDate returnDate,
            Model model) {

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        List<String> errors = new ArrayList<>();

        if (loanDate == null) {
            errors.add("La fecha de préstamo es obligatoria.");
        }

        if (returnDate == null) {
            errors.add("La fecha de devolución es obligatoria.");
        }

        if (loanDate != null && returnDate != null && returnDate.isBefore(loanDate)) {
            errors.add("La fecha de devolución no puede ser anterior a la fecha de préstamo.");
        }

        if (loanDate != null && loanDate.isAfter(java.time.LocalDate.now())) {
            errors.add("La fecha de préstamo no puede ser futura.");
        }

        boolean validStatus = false;
        for (Loan.Status s : Loan.Status.values()) {
            if (s.name().equals(status)) {
                validStatus = true;
                break;
            }
        }
        if (!validStatus) {
            errors.add("El estado seleccionado no es válido.");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("loan", loan);
            model.addAttribute("statuses", Loan.Status.values());
            return "admin/admin-edit-loans";
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

    @PostMapping("/loan/{id}/delete")
    public String deleteLoanAsAdmin(@PathVariable Long id) {
        loanRepository.deleteById(id);
        return "redirect:/admin/admin-panel#seccion-prestamos";
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
}