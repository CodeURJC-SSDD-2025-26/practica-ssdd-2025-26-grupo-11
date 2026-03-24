package es.codeurjc.practica2.controller;

import java.util.List;

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

    @GetMapping("/")
    public String showIndex(Model model) {
        List<Book> books = bookService.findAll();
        model.addAttribute("featuredBooks", books);
        return "index";
    }

    @GetMapping("/books")
    public String showBooks(Model model) {
        List<Book> books = bookService.findAll();
        model.addAttribute("books", books);
        return "books";
    }

    @GetMapping("/books/{id}")
    public String showBookDetail(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id).orElse(null);
        model.addAttribute("book", book);
        return "book-detail";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }
}