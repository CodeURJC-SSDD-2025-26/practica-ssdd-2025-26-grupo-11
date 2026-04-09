package es.codeurjc.practica2.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;
import es.codeurjc.practica2.model.Image;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.BookRepository;
import es.codeurjc.practica2.repository.LoanRepository;
import es.codeurjc.practica2.repository.ReviewRepository;
import es.codeurjc.practica2.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ReviewRepository reviewRepository;
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(BookRepository bookRepository, ImageService imageService, UserRepository userRepository,
            PasswordEncoder passwordEncoder, LoanRepository loanRepository, ReviewRepository reviewRepository) {
        this.bookRepository = bookRepository;
        this.imageService = imageService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loanRepository = loanRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) throws IOException {
        if (bookRepository.count() == 0) {
            Book book1 = new Book(
                    "El principito",
                    "Antoine de Saint-Exupéry",
                    "Libro clásico sobre la amistad y la imaginación.",
                    Genre.FICCION,
                    4.5f,
                    1943,
                    9780156012195L);
            setBookImage(book1, "static/images/logo.png");

            Book book2 = new Book(
                    "La casa de los espíritus",
                    "Isabel Allende",
                    "Saga familiar con realismo mágico.",
                    Genre.MISTERIO,
                    2.3f,
                    1982,
                    9781501117015L);
            setBookImage(book2, "static/images/logo.png");

            Book book3 = new Book(
                    "El código Da Vinci",
                    "Dan Brown",
                    "Thriller de misterio con ritmo rápido.",
                    Genre.MISTERIO,
                    3f,
                    2003,
                    9780307474278L);
            setBookImage(book3, "static/images/bookshelf-bg.jpg");

            Book book4 = new Book(
                    "Los detectives salvajes",
                    "Roberto Bolaño",
                    "Novela sobre poesía, juventud y aventura literaria.",
                    Genre.FICCION,
                    2.8f,
                    1998,
                    9780375400164L);
            setBookImage(book4, "static/images/default-avatar.png");

            User user1 = new User("user", "user", passwordEncoder.encode("pass"), "user@example.com", new Date(),
                    "USER");

            setUserImage(user1, "static/images/default-avatar.png");
            User admin = new User("admin", "admin", passwordEncoder.encode("adminpass"), "admin@example.com",
                    new Date(), "USER", "ADMIN");

            setUserImage(admin, "static/images/default-avatar.png");
            userRepository.save(user1);
            userRepository.save(admin);

            bookRepository.save(book1);
            bookRepository.save(book2);
            bookRepository.save(book3);
            bookRepository.save(book4);

            Review review1 = new Review("Muy chulo", 4, user1, book1);
            user1.addReview(review1);
            book1.addReview(review1);

            reviewRepository.save(review1);

            Loan loan1 = new Loan(
                    LocalDate.now().minusDays(5),
                    LocalDate.now().plusDays(10),
                    Loan.Status.ACTIVO,
                    user1,
                    book1
            );

            Loan loan2 = new Loan(
                    LocalDate.now().minusDays(20),
                    LocalDate.now().minusDays(5),
                    Loan.Status.VENCIDO,
                    user1,
                    book2
            );

            loanRepository.save(loan1);
            loanRepository.save(loan2);
        }
    }

    public void setBookImage(Book book, String classpathResource) throws IOException {
        Resource image = new ClassPathResource(classpathResource);
        Image createdImage = imageService.createImage(image.getInputStream());
        book.setImage(createdImage);
    }

    public void setUserImage(User user, String classpathResource) throws IOException {
        Resource image = new ClassPathResource(classpathResource);
        Image createdImage = imageService.createImage(image.getInputStream());
        user.setImage(createdImage);
    }
}
