package es.codeurjc.practica2.restController;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import es.codeurjc.practica2.dto.BookDTO;
import es.codeurjc.practica2.dto.DtoMapper;
import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.LoanService;

@RestController
@RequestMapping("/api/v1/books")
public class BookRestController {

    @Autowired
    private BookService bookService;

    @Autowired
    private LoanService loanService;

    @GetMapping
    public List<BookDTO> getBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String availability) {

        String normalizedQuery = (q == null || q.isBlank())
                ? null
                : q.trim().toLowerCase(Locale.ROOT);

        Genre genreEnum = parseGenre(genre);

        List<Book> books = bookService.searchBooks(normalizedQuery, genreEnum);

        return books.stream()
                .filter(book -> matchesAvailability(book, availability))
                .map(book -> DtoMapper.toBookDTO(book, loanService.isBookAvailable(book)))
                .toList();
    }

    @GetMapping("/{id}")
    public BookDTO getBook(@PathVariable Long id) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        boolean available = loanService.isBookAvailable(book);

        return DtoMapper.toBookDTO(book, available);
    }

    private Genre parseGenre(String genre) {
        if (genre == null || genre.isBlank()) {
            return null;
        }

        try {
            return Genre.valueOf(genre.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid genre");
        }
    }

    private boolean matchesAvailability(Book book, String availability) {
        if (availability == null || availability.isBlank()) {
            return true;
        }

        boolean available = loanService.isBookAvailable(book);

        if ("available".equalsIgnoreCase(availability)) {
            return available;
        }

        if ("loaned".equalsIgnoreCase(availability)) {
            return !available;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid availability filter");
    }
}