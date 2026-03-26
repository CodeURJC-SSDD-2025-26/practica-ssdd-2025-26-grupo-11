package es.codeurjc.practica2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.sql.Blob;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;

import java.util.Optional;
import java.sql.SQLException;
import java.util.List;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Image;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private ImageService imageService;

    // Get first 4 most rated books
    @GetMapping("/")
    public String showIndex(Model model, HttpServletRequest request) {

        // Si hay usuario logueado, redirige a /base
        if (request.getUserPrincipal() != null) {
            return "redirect:/base";
        }

        model.addAttribute("featuredBooks", bookService.findTopRatedBooks());
        return "index";
    }

    @GetMapping("/books")
    public String showBooks(Model model) {
        List<Book> books = bookService.findAll();
        model.addAttribute("books", books);
        return "books";
    }

    @GetMapping("/book-detail/{id}")
    public String showBookDetail(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id).orElse(null);
        model.addAttribute("title", book.getTitle());
        model.addAttribute("author", book.getAuthor());
        model.addAttribute("description", book.getDescription());
        model.addAttribute("rating", book.getRating());
        return "book-detail";
    }

    // GET BOOK BY ID
    @GetMapping("/book/{id}")
    public String getBookById(Model model, @PathVariable Long id) {
        model.addAttribute("book", bookService.findById(id).orElse(null));
        return "book";
    }

    @PostMapping("/admin/admin-add-book")
    public String newBookProcess(Model model, Book book, MultipartFile imageField,
            @RequestParam int year) throws IOException {

        if (!imageField.isEmpty()) {
            Image image = imageService.createImage(imageField.getInputStream());
            book.setImage(image);
        }

        bookService.save(book);

        model.addAttribute("id", book.getId());

        model.addAttribute("date", year);

        return "redirect:/book/" + book.getId();
    }

    // DELETE BOOK
    @DeleteMapping("/book/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteById(id);
    }

    private void updateImage(Book book, boolean removeImage, MultipartFile imageField)
            throws IOException, SQLException {

        if (!imageField.isEmpty()) {
            Book dbBook = bookService.findById(book.getId()).orElseThrow();

            if (dbBook.getImage() == null) {
                Image image = imageService.createImage(imageField.getInputStream());
                book.setImage(image);
            } else {
                Image image = imageService.replaceImageFile(dbBook.getImage().getId(), imageField.getInputStream());
                book.setImage(image);
            }
        } else {
            if (removeImage) {
                if (book.getImage() != null) {
                    imageService.deleteImage(book.getImage().getId());
                    book.setImage(null);
                }
            } else {
                // Maintain the same image loading it before updating the book
                Book dbBook = bookService.findById(book.getId()).orElseThrow();
                book.setImage(dbBook.getImage());
            }
        }
    }
}
