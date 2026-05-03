package es.codeurjc.practica2.restController;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.practica2.dto.DtoMapper;
import es.codeurjc.practica2.dto.ReviewCreateDTO;
import es.codeurjc.practica2.dto.ReviewDTO;
import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.ReviewService;
import es.codeurjc.practica2.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class ReviewRestController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    // -------------------------------------------------------
    // GET /api/v1/reviews
    // Admin: list all reviews, with optional search
    // -------------------------------------------------------
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewDTO>> getReviews(
            @RequestParam(required = false) String q,
            HttpServletRequest request) {

        if (!request.isUserInRole("ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo los administradores pueden listar todas las reseñas.");
        }

        List<ReviewDTO> reviews = reviewService.searchReviews(q).stream()
                .map(DtoMapper::toReviewDTO)
                .toList();

        return ResponseEntity.ok(reviews);
    }

    // -------------------------------------------------------
    // GET /api/v1/reviews/{id}
    // Admin or owner: get one review
    // -------------------------------------------------------
    @GetMapping("/reviews/{id}")
    public ResponseEntity<ReviewDTO> getReview(
            @PathVariable Long id,
            HttpServletRequest request) {

        Review review = findReviewOrThrow(id);
        User currentUser = getCurrentUser(request);

        boolean isAdmin = request.isUserInRole("ADMIN");
        boolean isOwner = review.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para consultar esta reseña.");
        }

        return ResponseEntity.ok(DtoMapper.toReviewDTO(review));
    }

    // -------------------------------------------------------
    // POST /api/v1/books/{bookId}/reviews
    // User: create a review for a book
    // -------------------------------------------------------
    @PostMapping("/books/{bookId}/reviews")
    public ResponseEntity<ReviewDTO> createReview(
            @PathVariable Long bookId,
            @RequestBody ReviewCreateDTO dto,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);

        Book book = bookService.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No se ha encontrado el libro indicado."));

        validateReview(dto);

        if (reviewService.findByUserAndBook(currentUser, book).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya has publicado una reseña para este libro.");
        }

        Review review = reviewService.addReview(
                bookId,
                currentUser.getId(),
                dto.comment(),
                dto.rating());

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/reviews/{id}")
                .buildAndExpand(review.getId())
                .toUri();

        return ResponseEntity.created(location).body(DtoMapper.toReviewDTO(review));
    }

    // -------------------------------------------------------
    // DELETE /api/v1/reviews/{id}
    // Admin or owner: delete review
    // -------------------------------------------------------
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            HttpServletRequest request) {

        Review review = findReviewOrThrow(id);
        User currentUser = getCurrentUser(request);

        boolean isAdmin = request.isUserInRole("ADMIN");
        boolean isOwner = review.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para eliminar esta reseña.");
        }

        reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------
    private User getCurrentUser(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();

        return userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No se ha encontrado el usuario autenticado."));
    }

    private Review findReviewOrThrow(Long id) {
        try {
            return reviewService.findById(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se ha encontrado la reseña indicada.");
        }
    }

    private void validateReview(ReviewCreateDTO dto) {
        if (dto.comment() == null || dto.comment().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El comentario es obligatorio.");
        }

        if (dto.comment().length() > 1500) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El comentario no puede superar los 1500 caracteres.");
        }

        if (dto.rating() < 1 || dto.rating() > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La puntuación debe estar entre 1 y 5.");
        }
    }
}
