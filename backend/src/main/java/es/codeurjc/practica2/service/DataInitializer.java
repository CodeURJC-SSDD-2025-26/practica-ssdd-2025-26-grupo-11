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
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.BookRepository;
import es.codeurjc.practica2.repository.LoanRepository;
import es.codeurjc.practica2.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReviewService reviewService;

    public DataInitializer(BookRepository bookRepository, ImageService imageService, UserRepository userRepository,
            PasswordEncoder passwordEncoder, LoanRepository loanRepository, ReviewService reviewService) {
        this.bookRepository = bookRepository;
        this.imageService = imageService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loanRepository = loanRepository;
        this.reviewService = reviewService;
    }

    @Override
    public void run(String... args) throws IOException {
        if (bookRepository.count() == 0) {
            Book book1 = new Book(
                    "El principito",
                    "Antoine de Saint-Exupéry",
                    "Libro clásico sobre la amistad y la imaginación.",
                    Genre.FICCION,
                    1943,
                    9780156012195L);
            setBookImage(book1, "static/images/el-principito.jpg");

            Book book2 = new Book(
                    "La casa de los espíritus",
                    "Isabel Allende",
                    "Saga familiar con realismo mágico.",
                    Genre.FANTASIA,
                    1982,
                    9781501117015L);
            setBookImage(book2, "static/images/la-casa-de-los-espiritus.jpg");

            Book book3 = new Book(
                    "El código Da Vinci",
                    "Dan Brown",
                    "Thriller de misterio con ritmo rápido.",
                    Genre.MISTERIO,
                    2003,
                    9780307474278L);
            setBookImage(book3, "static/images/el-codigo-davinci.jpg");

            Book book4 = new Book(
                    "Los detectives salvajes",
                    "Roberto Bolaño",
                    "Novela sobre poesía, juventud y aventura literaria.",
                    Genre.FICCION,
                    1998,
                    9780375400164L);
            setBookImage(book4, "static/images/los-detectives-salvajes.jpg");

            Book book5 = new Book(
                    "Cien años de soledad",
                    "Gabriel García Márquez",
                    "Obra maestra de la literatura latinoamericana con magia y realidad.",
                    Genre.FANTASIA,
                    1967,
                    9780060883287L);
            setBookImage(book5, "static/images/cien-anos-de-soledad.jpg");

            Book book6 = new Book(
                    "El Quijote",
                    "Miguel de Cervantes",
                    "Las aventuras de un caballero andante y su escudero.",
                    Genre.CLASICOS,
                    1605,
                    9788437604947L);
            setBookImage(book6, "static/images/don-quijote-de-la-mancha.jpg");

            Book book7 = new Book(
                    "La sombra del viento",
                    "Carlos Ruiz Zafón",
                    "Misterio y romance en la Barcelona post-guerra.",
                    Genre.MISTERIO,
                    2001,
                    9788432217357L);
            setBookImage(book7, "static/images/la-sombra-del-viento.jpg");

            Book book8 = new Book(
                    "El nombre de la rosa",
                    "Umberto Eco",
                    "Novela de misterio y filosofía en un monasterio medieval.",
                    Genre.MISTERIO,
                    1980,
                    9788433975645L);
            setBookImage(book8, "static/images/el-nombre-de-la-rosa.jpg");

            Book book9 = new Book(
                    "Orgullo y prejuicio",
                    "Jane Austen",
                    "Clásico de la literatura romántica inglesa.",
                    Genre.ROMANCE,
                    1813,
                    9780141439518L);
            setBookImage(book9, "static/images/orgullo-y-prejuicio.jpg");

            Book book10 = new Book(
                    "El viaje extraordinario",
                    "Jules Verne",
                    "Aventuras de exploración y descubrimiento.",
                    Genre.FANTASIA,
                    1874,
                    9788433929273L);
            setBookImage(book10, "static/images/el-viaje-extraordinario.jpg");

            User user1 = new User("user", "user", passwordEncoder.encode("pass"), "user@example.com", new Date(),
                    "USER");
            user1.setDescription("Amante de la lectura y de las buenas historias. Siempre busco nuevas recomendaciones.");
            setUserImage(user1, "static/images/default-avatar.png");
            
            User admin = new User("admin", "admin", passwordEncoder.encode("adminpass"), "admin@example.com",
                    new Date(), "USER", "ADMIN");
            admin.setDescription("Administrador de la plataforma BiblioOnline.");
            setUserImage(admin, "static/images/default-avatar.png");
            
            User user2 = new User("reader", "reader", passwordEncoder.encode("reader123"), "reader@example.com",
                    new Date(), "USER");
            user2.setDescription("Lectora ávida de clásicos de la literatura universal.");
            setUserImage(user2, "static/images/default-avatar.png");
            
            User user3 = new User("bookworm", "bookworm", passwordEncoder.encode("bookworm123"), "bookworm@example.com",
                    new Date(), "USER");
            user3.setDescription("Fanático de los misterios y las novelas de suspenso.");
            setUserImage(user3, "static/images/default-avatar.png");
            
            userRepository.save(user1);
            userRepository.save(admin);
            userRepository.save(user2);
            userRepository.save(user3);

            bookRepository.save(book1);
            bookRepository.save(book2);
            bookRepository.save(book3);
            bookRepository.save(book4);
            bookRepository.save(book5);
            bookRepository.save(book6);
            bookRepository.save(book7);
            bookRepository.save(book8);
            bookRepository.save(book9);
            bookRepository.save(book10);

            reviewService.addReview(book1.getId(), user1.getId(), "Muy chulo", 4);
            reviewService.addReview(book5.getId(), user2.getId(), "Una obra maestra imprescindible", 5);
            reviewService.addReview(book3.getId(), user3.getId(), "Muy entretenido, no pude dejarlo", 4);
            reviewService.addReview(book6.getId(), user1.getId(), "Clásico que vale la pena leer", 5);
            reviewService.addReview(book7.getId(), user2.getId(), "Misterio envolvente desde el principio", 4);
            reviewService.addReview(book8.getId(), user3.getId(), "Complejo pero fascinante", 3);
            reviewService.addReview(book9.getId(), admin.getId(), "Romántico y entretenido", 5);

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

            Loan loan3 = new Loan(
                    LocalDate.now().minusDays(3),
                    LocalDate.now().plusDays(14),
                    Loan.Status.ACTIVO,
                    user2,
                    book5
            );

            Loan loan4 = new Loan(
                    LocalDate.now().minusDays(15),
                    LocalDate.now(),
                    Loan.Status.VENCIDO,
                    user3,
                    book3
            );

            Loan loan5 = new Loan(
                    LocalDate.now().minusDays(2),
                    LocalDate.now().plusDays(20),
                    Loan.Status.ACTIVO,
                    user2,
                    book7
            );

            Loan loan6 = new Loan(
                    LocalDate.now().minusDays(10),
                    LocalDate.now().plusDays(5),
                    Loan.Status.ACTIVO,
                    user3,
                    book9
            );

            loanRepository.save(loan1);
            loanRepository.save(loan2);
            loanRepository.save(loan3);
            loanRepository.save(loan4);
            loanRepository.save(loan5);
            loanRepository.save(loan6);
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
