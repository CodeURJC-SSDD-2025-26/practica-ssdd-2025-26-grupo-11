package es.codeurjc.practica2.restController;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.practica2.dto.BookDTO;
import es.codeurjc.practica2.dto.BookRequestDTO;
import es.codeurjc.practica2.dto.DtoMapper;
import es.codeurjc.practica2.dto.ReviewDTO;
import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;
import es.codeurjc.practica2.model.Image;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.ImageService;
import es.codeurjc.practica2.service.LoanService;

@RestController
@RequestMapping("/api/v1/books")
public class BookRestController {

    @Autowired
    private BookService bookService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private ImageService imageService;


    // -------------------------------------------------------
    // GET /api/v1/books
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<Page<BookDTO>> getBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String availability,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String normalizedQuery = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);
        Genre genreEnum = parseGenre(genre);

        Page<Book> bookPage = bookService.searchBooksPageable(normalizedQuery, genreEnum, page, size);

        List<BookDTO> filtered = bookPage.getContent().stream()
                .map(book -> {
                    boolean available = loanService.isBookAvailable(book);
                    if (availability != null && !availability.isBlank()) {
                        if ("available".equalsIgnoreCase(availability) && !available) return null;
                        if ("loaned".equalsIgnoreCase(availability) && available) return null;
                        if (!availability.equalsIgnoreCase("available") && !availability.equalsIgnoreCase("loaned")) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid availability filter");
                        }
                    }
                    return DtoMapper.toBookDTO(book, available);
                })
                .filter(dto -> dto != null)
                .toList();

        Page<BookDTO> dtoPage = new PageImpl<>(filtered, bookPage.getPageable(), bookPage.getTotalElements());

        return ResponseEntity.ok(dtoPage);
    }

    // -------------------------------------------------------
    // GET /api/v1/books/{id}
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBook(@PathVariable Long id) {
        Book book = findBookOrThrow(id);
        return ResponseEntity.ok(DtoMapper.toBookDTO(book, loanService.isBookAvailable(book)));
    }

    // -------------------------------------------------------
    // POST /api/v1/books
    // -------------------------------------------------------
    @PostMapping
    public ResponseEntity<BookDTO> createBook(@RequestBody BookRequestDTO dto) throws IOException {
        validateBookRequest(dto);

        Book book = new Book(
                dto.title(),
                dto.author(),
                dto.description(),
                parseGenre(dto.genre()),
                dto.year(),
                dto.isbn());

        bookService.save(book);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(book.getId())
                .toUri();

        return ResponseEntity.created(location).body(DtoMapper.toBookDTO(book, true));
    }

    // -------------------------------------------------------
    // PUT /api/v1/books/{id}
    // -------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @RequestBody BookRequestDTO dto) {
        validateBookRequest(dto);

        Book book = findBookOrThrow(id);

        book.setTitle(dto.title());
        book.setAuthor(dto.author());
        book.setDescription(dto.description());
        book.setGenre(parseGenre(dto.genre()));
        book.setYear(dto.year());
        book.setIsbn(dto.isbn());

        bookService.save(book);

        return ResponseEntity.ok(DtoMapper.toBookDTO(book, loanService.isBookAvailable(book)));
    }

    // -------------------------------------------------------
    // DELETE /api/v1/books/{id}
    // -------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        findBookOrThrow(id);
        boolean deleted = loanService.deleteBookIfAllowed(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete book with active or overdue loans");
        }
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------
    // GET /api/v1/books/{id}/reviews
    // -------------------------------------------------------
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewDTO>> getBookReviews(@PathVariable Long id) {
        Book book = findBookOrThrow(id);
        List<ReviewDTO> reviews = book.getReviews().stream()
                .map(DtoMapper::toReviewDTO)
                .toList();
        return ResponseEntity.ok(reviews);
    }

    // -------------------------------------------------------
    // POST /api/v1/books/{id}/image/
    // -------------------------------------------------------
    @PostMapping("/{id}/image/")
    public ResponseEntity<BookDTO> createBookImage(
            @PathVariable Long id,
            @RequestParam MultipartFile imageFile) throws IOException {

        validateImageFile(imageFile);

        Book book = findBookOrThrow(id);

        Image image = imageService.createImage(imageFile.getInputStream());
        book.setImage(image);
        bookService.save(book);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/media")
                .build()
                .toUri();

        return ResponseEntity.created(location)
                .body(DtoMapper.toBookDTO(book, loanService.isBookAvailable(book)));
    }

    // -------------------------------------------------------
    // GET /api/v1/books/{id}/image/media
    // -------------------------------------------------------
    @GetMapping("/{id}/image/media")
    public ResponseEntity<Object> getBookImageMedia(@PathVariable Long id) throws SQLException {
        Book book = findBookOrThrow(id);

        if (book.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        Resource imageFile = imageService.getImageFile(book.getImage().getId());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageFile);
    }

    // -------------------------------------------------------
    // PUT /api/v1/books/{id}/image/media
    // -------------------------------------------------------
    @PutMapping("/{id}/image/media")
    public ResponseEntity<Void> replaceBookImage(
            @PathVariable Long id,
            @RequestParam MultipartFile imageFile) throws IOException {

        validateImageFile(imageFile);

        Book book = findBookOrThrow(id);

        if (book.getImage() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This book has no image to replace");
        }

        imageService.replaceImageFile(book.getImage().getId(), imageFile.getInputStream());

        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------
    // DELETE /api/v1/books/{id}/image/
    // -------------------------------------------------------
    @DeleteMapping("/{id}/image/")
    public ResponseEntity<Void> deleteBookImage(@PathVariable Long id) {
        Book book = findBookOrThrow(id);

        if (book.getImage() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This book has no image");
        }

        Long imageId = book.getImage().getId();
        book.setImage(null);
        bookService.save(book);
        imageService.deleteImage(imageId);

        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------
    private Book findBookOrThrow(Long id) {
        return bookService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    private Genre parseGenre(String genre) {
        if (genre == null || genre.isBlank()) return null;
        try {
            return Genre.valueOf(genre.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid genre: " + genre);
        }
    }

    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file cannot be empty");
        }
        String contentType = imageFile.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must be .jpg or .png");
        }
    }

    private void validateBookRequest(BookRequestDTO dto) {
        if (dto.title() == null || dto.title().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        if (dto.title().length() > 20)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title must be 20 characters or less");

        if (dto.author() == null || dto.author().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author is required");
        if (dto.author().length() > 25)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author must be 25 characters or less");

        if (dto.year() == null || dto.year() < 1000 || dto.year() > 2099)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year must be between 1000 and 2099");

        if (dto.isbn() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ISBN is required");

        if (dto.description() != null && dto.description().length() > 150)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description must be 150 characters or less");

        if (dto.genre() == null || dto.genre().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Genre is required");
    }
}