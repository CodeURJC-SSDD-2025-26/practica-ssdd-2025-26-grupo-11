package es.codeurjc.practica2.service;

import java.io.IOException;
import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Image;
import es.codeurjc.practica2.repository.BookRepository;
import es.codeurjc.practica2.repository.UserRepository;
import es.codeurjc.practica2.service.ImageService;
import org.springframework.core.io.Resource;
import es.codeurjc.practica2.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public DataInitializer(BookRepository bookRepository, ImageService imageService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.bookRepository = bookRepository;
        this.imageService = imageService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws IOException {
        if (bookRepository.count() == 0) {
            Book book1 = new Book(
                    "El principito",
                    "Antoine de Saint-Exupéry",
                    "Libro clásico sobre la amistad y la imaginación.",
                    "Fiction",
                    4.5f,
                    1943,
                    9780156012195L
            );

            setBookImage(book1, "static/images/logo.png");
            bookRepository.save(book1);

            Book book2 = new Book(
                    "La casa de los espíritus",
                    "Isabel Allende",
                    "Saga familiar con realismo mágico.",
                    "Drama",
                    2.3f,
                        1982,
                    9781501117015L
            );
            setBookImage(book2, "static/images/logo.png");
            bookRepository.save(book2);

            Book book3 = new Book(
                    "El código Da Vinci",
                    "Dan Brown",
                    "Thriller de misterio con ritmo rápido.",
                    "Mystery",
                    3f,
                    2003,
                    9780307474278L
            );
            setBookImage(book3, "static/images/bookshelf-bg.jpg");
            bookRepository.save(book3);

            Book book4 = new Book(
                    "Los detectives salvajes",
                    "Roberto Bolaño",
                    "Novela sobre poesía, juventud y aventura literaria.",
                    "Literary Fiction",
                    2.8f,
                    1998,
                    9780375400164L
            );
            setBookImage(book4, "static/images/default-avatar.png");
            bookRepository.save(book4);

            userRepository.save(new User("user", "user", passwordEncoder.encode("pass"), "user@example.com", new Date(), "USER"));
		    userRepository.save(new User("admin", "admin", passwordEncoder.encode("adminpass"), "admin@example.com", new Date(), "USER", "ADMIN"));
        }
    }

    public void setBookImage(Book book, String classpathResource) throws IOException{
		Resource image = new ClassPathResource(classpathResource);
        Image createdImage = imageService.createImage(image.getInputStream());
        book.setImage(createdImage);
	}
}