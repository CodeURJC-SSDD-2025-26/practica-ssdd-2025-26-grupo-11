package es.codeurjc.practica2.service;
 
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
 
import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;
import es.codeurjc.practica2.model.Image;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.BookRepository;
import es.codeurjc.practica2.repository.UserRepository;
 
@Service
public class UserService {
 
    @Autowired
    private UserRepository userRepository;
 
    @Autowired
    private PasswordEncoder passwordEncoder;
 
    @Autowired
    private BookRepository bookRepository;
 
    @Autowired
    private ImageService imageService;
 
    // -------------------------------------------------------------------------
    // Basic queries
    // -------------------------------------------------------------------------
 
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
 
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
 
    public List<User> findAll() {
        return userRepository.findAll();
    }
 
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
 
    // -------------------------------------------------------------------------
    // Registration (moved validation from UserController)
    // -------------------------------------------------------------------------
 
    /**
     * Validates registration fields.
     * Returns a list of error messages. Empty list means all fields are valid.
     */
    public List<String> validateRegistration(String name, String surname, String email,
            String password, String confirmPassword) {
 
        List<String> errors = new ArrayList<>();
 
        if (name == null || name.isBlank()) {
            errors.add("El nombre es obligatorio.");
        } else if (name.length() > 22) {
            errors.add("El nombre no puede superar los 22 caracteres.");
        }
 
        if (surname == null || surname.isBlank()) {
            errors.add("El apellido es obligatorio.");
        } else if (surname.length() > 22) {
            errors.add("El apellido no puede superar los 22 caracteres.");
        }
 
        if (email == null || email.isBlank()) {
            errors.add("El email es obligatorio.");
        } else if (email.length() > 30) {
            errors.add("El email no puede superar los 30 caracteres.");
        } else if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.add("El formato del email no es válido.");
        }
 
        if (password == null || password.isBlank()) {
            errors.add("La contraseña es obligatoria.");
        } else if (password.length() < 6) {
            errors.add("La contraseña debe tener al menos 6 caracteres.");
        } else if (password.length() > 30) {
            errors.add("La contraseña no puede superar los 30 caracteres.");
        }
 
        if (confirmPassword == null || confirmPassword.isBlank()) {
            errors.add("Debes confirmar la contraseña.");
        } else if (!password.equals(confirmPassword)) {
            errors.add("Las contraseñas no coinciden.");
        }
 
        if (email != null && !email.isBlank() && emailExists(email)) {
            errors.add("Este correo ya está registrado.");
        }
 
        return errors;
    }
 
    /**
     * Creates a new USER-role user and assigns the default avatar image.
     */
    public User registerUser(String name, String surname, String email, String rawPassword)
            throws IOException {
 
        User user = new User(
                name,
                surname,
                passwordEncoder.encode(rawPassword),
                email,
                new Date(),
                "USER"
        );
 
        userRepository.save(user);
 
        Resource resource = new ClassPathResource("static/images/default-avatar.png");
        if (resource.exists()) {
            Image image = imageService.createImage(resource.getInputStream());
            user.setImage(image);
            userRepository.save(user);
        }
 
        return user;
    }
 
    // -------------------------------------------------------------------------
    // Profile update (moved from UserController)
    // -------------------------------------------------------------------------
 
    /**
     * Updates a user's profile fields and optionally replaces the avatar image.
     */
    public void updateProfile(Long userId, String name, String surname, String email,
            String bio, MultipartFile imageFile) throws IOException {
 
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setDescription(bio);
 
        if (imageFile != null && !imageFile.isEmpty()) {
            InputStream stream = imageFile.getInputStream();
            if (user.getImage() != null) {
                imageService.replaceImageFile(user.getImage().getId(), stream);
            } else {
                Image image = imageService.createImage(stream);
                user.setImage(image);
            }
        }
 
        userRepository.save(user);
    }
 
    // -------------------------------------------------------------------------
    // Admin: delete user (moved from AdminController)
    // -------------------------------------------------------------------------
 
    /**
     * Deletes a user together with their reviews and avatar image.
     * The caller must ensure the user is not the currently logged-in admin.
     */
    public void deleteUser(Long userId, ReviewService reviewService) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        reviewService.deleteUserReviews(user);
 
        if (user.getImage() != null) {
            Long imageId = user.getImage().getId();
            user.setImage(null);
            userRepository.save(user);
            imageService.deleteImage(imageId);
        }
 
        userRepository.delete(user);
    }
 
    // -------------------------------------------------------------------------
    // Recommendations algorithm (unchanged, already lived here)
    // -------------------------------------------------------------------------
 
    public List<Book> getRecommendedBooks(User user) {
        List<Loan> userLoans = user.getLoans();
 
        if (userLoans == null || userLoans.isEmpty()) {
            return new ArrayList<>();
        }
 
        List<Genre> allGenres = new ArrayList<>();
        for (Loan loan : userLoans) {
            Genre genre = loan.getBook().getGenre();
            if (genre != null) {
                allGenres.add(genre);
            }
        }
 
        if (allGenres.isEmpty()) {
            return new ArrayList<>();
        }
 
        Genre favoriteGenre = allGenres.get(0);
        int maxCount = 0;
 
        for (int i = 0; i < allGenres.size(); i++) {
            Genre currentGenre = allGenres.get(i);
            int currentCount = 0;
            for (int j = 0; j < allGenres.size(); j++) {
                if (allGenres.get(j) == currentGenre) {
                    currentCount++;
                }
            }
            if (currentCount > maxCount) {
                maxCount = currentCount;
                favoriteGenre = currentGenre;
            }
        }
 
        List<Book> booksOfGenre = bookRepository.findByGenreOrderByRatingDesc(favoriteGenre);
        List<Book> recommendations = new ArrayList<>();
 
        for (int i = 0; i < booksOfGenre.size() && recommendations.size() < 3; i++) {
            Book current = booksOfGenre.get(i);
            boolean alreadyRead = false;
            for (int j = 0; j < userLoans.size() && !alreadyRead; j++) {
                if (userLoans.get(j).getBook().getId().equals(current.getId())) {
                    alreadyRead = true;
                }
            }
            if (!alreadyRead) {
                recommendations.add(current);
            }
        }
 
        return recommendations;
    }
}