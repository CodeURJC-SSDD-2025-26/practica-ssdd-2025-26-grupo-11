package es.codeurjc.practica2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.service.BookService;

@RestController
@RequestMapping("/api/books")
@CrossOrigin
public class BookController {

    @Autowired
    private BookService bookService;

    // GET ALL BOOKS
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.findAll();
    }

    // GET BOOK BY ID
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.findById(id).orElse(null);
    }

    // CREATE BOOK
    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return bookService.save(book);
    }

    // DELETE BOOK
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteById(id);
    }
}
