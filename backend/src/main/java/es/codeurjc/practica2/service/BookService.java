package es.codeurjc.practica2.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;
import es.codeurjc.practica2.model.GenreSection;
import es.codeurjc.practica2.model.Image;
import es.codeurjc.practica2.model.PageData;
import es.codeurjc.practica2.repository.BookRepository;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ImageService imageService;

    // -------------------------------------------------------------------------
    // Basic CRUD
    // -------------------------------------------------------------------------

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public boolean exist(long id) {
        return bookRepository.existsById(id);
    }

    public void save(Book book) {
        bookRepository.save(book);
    }

    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public List<Book> findTopRatedBooks() {
        return bookRepository.findTop4ByOrderByRatingDesc();
    }

    public List<Book> searchBooks(String q, Genre genre) {
        return bookRepository.searchBooks(q, genre);
    }

    public List<Object[]> avgRatingByGenre() {
        return bookRepository.avgRatingByGenre();
    }

    /**
     * Admin panel: paginated book search.
     */
    public PageData<Book> searchBooksPage(String q, String genre, int page, int size,
            LoanService loanService) {
        String param = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);
        Genre genreEnum = (genre == null || genre.isBlank()) ? null : Genre.valueOf(genre.toUpperCase());

        Page<Book> result = bookRepository.searchBooksPage(
                param, genreEnum, PageRequest.of(page, size, Sort.by("title")));

        for (Book book : result.getContent()) {
            book.setAvailable(loanService.isBookAvailable(book));
        }

        return new PageData<>(result.getContent(), page,
                result.getTotalPages(), result.getTotalElements(), size);
    }

    // -------------------------------------------------------------------------
    // Business logic moved from BookController
    // -------------------------------------------------------------------------

    /**
     * Builds the list of genre sections shown in /books, applying search,
     * genre and availability filters.
     */
    public List<GenreSection> getBookSections(String q, String genre, String availability,
            LoanService loanService) {

        String normalizedQuery = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);
        Genre genreEnum = (genre == null || genre.isBlank()) ? null : Genre.valueOf(genre);

        List<Book> books = searchBooks(normalizedQuery, genreEnum);

        for (Book book : books) {
            book.setAvailable(loanService.isBookAvailable(book));
        }

        String selectedAvailability = availability == null ? "" : availability.trim();

        List<Book> filteredBooks = books.stream()
                .filter(book -> {
                    if (selectedAvailability.isBlank()) return true;
                    if ("available".equals(selectedAvailability)) return book.isAvailable();
                    if ("loaned".equals(selectedAvailability)) return !book.isAvailable();
                    return true;
                })
                .toList();

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

        return sections;
    }

    /**
     * Builds the star icon list for a given rating (used in book-detail view).
     */
    public List<String> buildStarList(float rating) {
        List<String> stars = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            if (rating >= i) {
                stars.add("fa-solid fa-star");
            } else if (rating >= i - 0.5) {
                stars.add("fa-solid fa-star-half-stroke");
            } else {
                stars.add("fa-regular fa-star");
            }
        }
        return stars;
    }

    // -------------------------------------------------------------------------
    // Business logic moved from AdminController
    // -------------------------------------------------------------------------

    /**
     * Creates a new book validating fields and saving the image.
     * Returns a list of validation errors (empty means success).
     */
    public List<String> createBook(Book book, String genreString, MultipartFile imageField,
            String yearStr, String isbnStr) throws IOException {

        List<String> errors = validateBookFields(book, genreString, yearStr, isbnStr, imageField);
        if (!errors.isEmpty()) {
            return errors;
        }

        book.setGenre(Genre.valueOf(genreString.toUpperCase()));
        book.setImage(resolveBookImage(null, false, imageField));
        save(book);
        return errors; // empty
    }

    /**
     * Updates an existing book validating fields and updating the image.
     * Returns a list of validation errors (empty means success).
     */
    public List<String> updateBook(Long id, Book updatedBook, String genreString,
            boolean removeImage, MultipartFile imageField,
            String yearStr, String isbnStr) throws IOException, SQLException {

        Book dbBook = findById(id).orElseThrow(() -> new RuntimeException("Book not found"));

        List<String> errors = validateBookFields(updatedBook, genreString, yearStr, isbnStr, imageField);
        if (!errors.isEmpty()) {
            return errors;
        }

        dbBook.setTitle(updatedBook.getTitle());
        dbBook.setAuthor(updatedBook.getAuthor());
        dbBook.setDescription(updatedBook.getDescription());
        dbBook.setYear(updatedBook.getYear());
        dbBook.setIsbn(updatedBook.getIsbn());
        dbBook.setGenre(Genre.valueOf(genreString.toUpperCase()));
        dbBook.setImage(resolveBookImage(dbBook, removeImage, imageField));
        save(dbBook);
        return errors; // empty
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<String> validateBookFields(Book book, String genreString, String yearStr,
            String isbnStr, MultipartFile imageField) {

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

        if (genreString == null || genreString.isBlank()) {
            errors.add("El género es obligatorio.");
        } else {
            try {
                Genre.valueOf(genreString.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("El género seleccionado no es válido.");
            }
        }

        if (imageField != null && !imageField.isEmpty()) {
            String contentType = imageField.getContentType();
            if (contentType == null
                    || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                errors.add("La imagen debe ser .jpg o .png.");
            }
        }

        return errors;
    }

    /**
     * Resolves which image a book should have after a create/update operation.
     * - If a new file is uploaded it is saved (replacing the old one if any).
     * - If removeImage is true the current image is deleted.
     * - Otherwise the existing image is kept.
     * - On creation with no file a default image is assigned.
     */
    private Image resolveBookImage(Book existingBook, boolean removeImage,
            MultipartFile imageField) throws IOException {

        if (imageField != null && !imageField.isEmpty()) {
            InputStream stream = imageField.getInputStream();
            if (existingBook != null && existingBook.getImage() != null) {
                return imageService.replaceImageFile(existingBook.getImage().getId(), stream);
            }
            return imageService.createImage(stream);
        }

        if (removeImage) {
            if (existingBook != null && existingBook.getImage() != null) {
                imageService.deleteImage(existingBook.getImage().getId());
            }
            return null;
        }

        if (existingBook != null) {
            // Keep current image on edit with no new file
            return existingBook.getImage();
        }

        // Creation with no file: use default
        Resource defaultImage = new ClassPathResource("static/images/default-book.png");
        return imageService.createImage(defaultImage.getInputStream());
    }
}