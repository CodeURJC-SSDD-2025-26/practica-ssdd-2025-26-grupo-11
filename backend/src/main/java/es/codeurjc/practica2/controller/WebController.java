package es.codeurjc.practica2.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.service.BookService;

@Controller
public class WebController {

    @Autowired
    private BookService bookService;

    // Init page path
    @GetMapping("/")
    public String index(Model model) {
        List<Book> featured = bookService.findTopRatedBooks();
        model.addAttribute("featuredBooks", featured);
        
        return "index";
    }

    // Book detail path
    @GetMapping("/book-detail/{id}")
    public String bookDetail(Model model, @PathVariable Long id) {
        Optional<Book> book = bookService.findById(id);

        if (book.isPresent()) {
            model.addAttribute("book", book.get());
            return "book-detail";
        } else {
            //página de error
            return null;
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";  
    }
    
    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    // Ruta para ver el catálogo completo de libros
    @GetMapping("/books")
    public String showAllBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "books";
    }
}