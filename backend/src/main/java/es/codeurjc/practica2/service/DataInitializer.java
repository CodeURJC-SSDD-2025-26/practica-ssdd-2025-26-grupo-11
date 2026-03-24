package es.codeurjc.practica2.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.repository.BookRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;

    public DataInitializer(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() == 0) {
            bookRepository.save(new Book(
                    "El principito",
                    "Antoine de Saint-Exupéry",
                    "Libro clásico sobre la amistad y la imaginación.",
                    "Fiction"
            ));

            bookRepository.save(new Book(
                    "La casa de los espíritus",
                    "Isabel Allende",
                    "Saga familiar con realismo mágico.",
                    "Drama"
            ));

            bookRepository.save(new Book(
                    "El código Da Vinci",
                    "Dan Brown",
                    "Thriller de misterio con ritmo rápido.",
                    "Mystery"
            ));

            bookRepository.save(new Book(
                    "Los detectives salvajes",
                    "Roberto Bolaño",
                    "Novela sobre poesía, juventud y aventura literaria.",
                    "Literary Fiction"
            ));
        }
    }
}