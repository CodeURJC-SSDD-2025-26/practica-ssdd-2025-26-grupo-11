package es.codeurjc.practica2.DTO;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.model.User;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static BookDTO toBookDTO(Book book, boolean available) {
        return new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getGenre() != null ? book.getGenre().name() : null,
                book.getGenreDisplayName(),
                book.getRating(),
                book.getYear(),
                book.getIsbn(),
                available,
                book.getImage() != null ? "/image/" + book.getImage().getId() : null
        );
    }

    public static UserDTO toUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getDescription(),
                user.getImage() != null ? "/users/" + user.getId() + "/image" : null
        );
    }

    public static LoanDTO toLoanDTO(Loan loan) {
        User user = loan.getUser();
        Book book = loan.getBook();

        return new LoanDTO(
                loan.getId(),
                loan.getLoanDate(),
                loan.getReturnDate(),
                loan.getStatus() != null ? loan.getStatus().name() : null,
                user != null ? user.getId() : null,
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                book != null ? book.getId() : null,
                book != null ? book.getTitle() : null
        );
    }

    public static ReviewDTO toReviewDTO(Review review) {
        User user = review.getUser();
        Book book = review.getBook();

        return new ReviewDTO(
                review.getId(),
                review.getComment(),
                review.getRating(),
                user != null ? user.getId() : null,
                user != null ? user.getName() : null,
                book != null ? book.getId() : null,
                book != null ? book.getTitle() : null
        );
    }
}