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
    
    // If user has no loans, return empty list
    if (userLoans == null || userLoans.isEmpty()) {
        return new ArrayList<>();
    }

    // 1. Extract all genres into a list
    List<Genre> allGenres = new ArrayList<>();
    for (int i = 0; i < userLoans.size(); i++) {
        Genre genre = userLoans.get(i).getBook().getGenre();
        if (genre != null) {
            allGenres.add(genre);
        }
    }

    // If there are no valid genres, return empty list
    if (allGenres.isEmpty()) {
        return new ArrayList<>();
    }

    // 2. Find the most repeated genre (Nested loop to count)
    Genre favoriteGenre = allGenres.get(0);
    int maxCount = 0;

    for (int i = 0; i < allGenres.size(); i++) {
        Genre currentGenre = allGenres.get(i);
        int currentCount = 0;

        // Count how many times the current genre appears in the entire list
        for (int j = 0; j < allGenres.size(); j++) {
            if (allGenres.get(j) == currentGenre) {
                currentCount++;
            }
        }

        // If this genre appears more times than the previous maximum, save it
        if (currentCount > maxCount) {
            maxCount = currentCount;
            favoriteGenre = currentGenre;
        }
    }

    // 3. Search for books of that genre in the database
    List<Book> booksOfGenre = bookRepository.findByGenreOrderByRatingDesc(favoriteGenre);
    List<Book> finalRecommendations = new ArrayList<>();

    // 4. Filter books not read (Without using break)
    // The loop stops if we run out of books or if we already have 3 recommendations
    for (int i = 0; i < booksOfGenre.size() && finalRecommendations.size() < 3; i++) {
        Book currentBook = booksOfGenre.get(i);
        boolean alreadyRead = false;

        // Check if user has read it. The condition "!alreadyRead" makes
        // the loop stop automatically if found, acting as a natural "break".
        for (int j = 0; j < userLoans.size() && !alreadyRead; j++) {
            if (userLoans.get(j).getBook().getId().equals(currentBook.getId())) {
                alreadyRead = true;
            }
        }

        // If after reviewing all their loans they haven't read it, we recommend it
        if (!alreadyRead) {
            finalRecommendations.add(currentBook);
        }
    }

    return finalRecommendations;
}
}