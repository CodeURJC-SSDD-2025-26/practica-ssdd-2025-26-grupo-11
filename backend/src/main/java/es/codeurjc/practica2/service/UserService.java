package es.codeurjc.practica2.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;
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

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User registerUser(String name, String surname, String email, String rawPassword) {
        User user = new User(
                name,
                surname,
                passwordEncoder.encode(rawPassword),
                email,
                new Date(),
                "USER"
        );

        return userRepository.save(user);
    }

    public List<Book> getRecommendedBooks(User user) {
    List<Loan> userLoans = user.getLoans();
    
    // Si no tiene préstamos, devolvemos una lista vacía
    if (userLoans == null || userLoans.isEmpty()) {
        return new ArrayList<>();
    }

    // 1. Extraer todos los géneros a una lista
    List<Genre> allGenres = new ArrayList<>();
    for (int i = 0; i < userLoans.size(); i++) {
        Genre genre = userLoans.get(i).getBook().getGenre();
        if (genre != null) {
            allGenres.add(genre);
        }
    }

    // Si no hay géneros válidos, retornar lista vacía
    if (allGenres.isEmpty()) {
        return new ArrayList<>();
    }

    // 2. Buscar el género que más se repite (Bucle anidado para contar)
    Genre favoriteGenre = allGenres.get(0);
    int maxCount = 0;

    for (int i = 0; i < allGenres.size(); i++) {
        Genre currentGenre = allGenres.get(i);
        int currentCount = 0;

        // Contamos cuántas veces aparece el género actual en toda la lista
        for (int j = 0; j < allGenres.size(); j++) {
            if (allGenres.get(j) == currentGenre) {
                currentCount++;
            }
        }

        // Si este género aparece más veces que el máximo anterior, lo guardamos
        if (currentCount > maxCount) {
            maxCount = currentCount;
            favoriteGenre = currentGenre;
        }
    }

    // 3. Buscar libros de ese género en la base de datos
    List<Book> booksOfGenre = bookRepository.findByGenreOrderByRatingDesc(favoriteGenre);
    List<Book> finalRecommendations = new ArrayList<>();

    // 4. Filtrar los no leídos (Sin usar break)
    // El bucle se detiene si nos quedamos sin libros o si ya tenemos 3 recomendaciones
    for (int i = 0; i < booksOfGenre.size() && finalRecommendations.size() < 3; i++) {
        Book currentBook = booksOfGenre.get(i);
        boolean alreadyRead = false;

        // Comprobamos si lo ha leído. La condición "!alreadyRead" hace que 
        // el bucle pare automáticamente si lo encuentra, actuando como un "break" natural.
        for (int j = 0; j < userLoans.size() && !alreadyRead; j++) {
            if (userLoans.get(j).getBook().getId().equals(currentBook.getId())) {
                alreadyRead = true;
            }
        }

        // Si después de revisar todos sus préstamos no lo ha leído, lo recomendamos
        if (!alreadyRead) {
            finalRecommendations.add(currentBook);
        }
    }

    return finalRecommendations;
}
}