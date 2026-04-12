package es.codeurjc.practica2.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Genre;
import es.codeurjc.practica2.model.Image;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.repository.BookRepository;
import es.codeurjc.practica2.repository.ImageRepository;
import es.codeurjc.practica2.repository.LoanRepository;
import es.codeurjc.practica2.repository.ReviewRepository;
import es.codeurjc.practica2.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ImageRepository imageRepository;

    //Main load
    @GetMapping("/admin-panel")
    public String showAdminPanel(Model model) {
        
        model.addAttribute("booksCount", bookRepository.count());
        model.addAttribute("usersCount", userRepository.count());
        model.addAttribute("reviewsCount", reviewRepository.count());

        List<Loan> allLoans = loanRepository.findAll();
        long activeLoans = allLoans.stream().filter(Loan::isActive).count();
        long overdueLoans = allLoans.stream().filter(Loan::isOverdue).count();
        long returnedLoans = allLoans.stream().filter(Loan::isReturned).count();

        model.addAttribute("activeLoansCount", activeLoans);
        model.addAttribute("overdueLoansCount", overdueLoans);
        model.addAttribute("returnedLoansCount", returnedLoans);

        model.addAttribute("books", bookRepository.findAll());
        model.addAttribute("loans", allLoans);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("reviews", reviewRepository.findAll());

        model.addAttribute("genreLabelsJson", "[]");
        model.addAttribute("genreLoanCountsJson", "[]");
        model.addAttribute("genreRatingLabelsJson", "[]");
        model.addAttribute("genreRatingValuesJson", "[]");

        return "admin-panel";
    }

    // Add new book
    @GetMapping("/admin-add-book")
    public String showAddBookForm() {
        return "admin-add-book";
    }

    @PostMapping("/admin-add-book")
    public String processAddBook(
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam int year, 
            @RequestParam String genre,
            @RequestParam long isbn, 
            @RequestParam String description,
            @RequestParam MultipartFile imageField) throws IOException, SQLException {

        Book newBook = new Book();
        newBook.setTitle(title);
        newBook.setAuthor(author);
        newBook.setYear(year); 
        newBook.setIsbn(isbn);
        newBook.setDescription(description);
        newBook.setRating(0.0f); 
        
      
        newBook.setGenre(Genre.valueOf(genre));

        // Image treat
        if (imageField != null && !imageField.isEmpty()) {
            Image img = new Image();
            img.setImageFile(new SerialBlob(imageField.getBytes()));
            
            // We save the imagen in its table first 
            imageRepository.save(img); 
            
            newBook.setImage(img);
        }

        // We save the book in the data base
        bookRepository.save(newBook);

        return "redirect:/admin/admin-panel#seccion-libros";
    }

    //Edit book

    // Load form with current data
    @GetMapping("/admin-edit-book/{id}")
    public String showEditBookForm(@PathVariable Long id, Model model) {
        Book book = bookRepository.findById(id).orElseThrow();
        
        model.addAttribute("book", book);
        
        // Genre in a String
        if (book.getGenre() != null) {
            model.addAttribute("currentGenre", book.getGenre().name());
        }
        
        return "admin-edit-book";
    }

    // Get and process changes
    @PostMapping("/admin-edit-book/{id}")
    public String processEditBook(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam int year, 
            @RequestParam String genre,
            @RequestParam long isbn,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile imageField) throws IOException, SQLException {

        // Look for the book
        Book existingBook = bookRepository.findById(id).orElseThrow();

        // Update data
        existingBook.setTitle(title);
        existingBook.setAuthor(author);
        existingBook.setYear(year);
        existingBook.setIsbn(isbn);
        existingBook.setDescription(description);
        existingBook.setGenre(Genre.valueOf(genre));

        // Image
        if (imageField != null && !imageField.isEmpty()) {
            
            if (existingBook.getImage() != null) {
                Image oldImage = existingBook.getImage();
                oldImage.setImageFile(new SerialBlob(imageField.getBytes()));
                imageRepository.save(oldImage);
            } 
            // We create new image if it didn't have one
            else {
                Image newImage = new Image();
                newImage.setImageFile(new SerialBlob(imageField.getBytes()));
                imageRepository.save(newImage);
                existingBook.setImage(newImage);
            }
        }

        //Save changes
        bookRepository.save(existingBook);

        return "redirect:/admin/admin-panel#seccion-libros";
    }

    //Admin actions

    @PostMapping("/loan/{id}/return")
    public String returnLoanAsAdmin(@PathVariable Long id) {
        Loan loan = loanRepository.findById(id).orElseThrow();
        loan.setStatus(Loan.Status.DEVUELTO);
        loanRepository.save(loan);
        return "redirect:/admin/admin-panel#seccion-prestamos";
    }

    @PostMapping("/review/{id}/delete")
    public String deleteReviewAsAdmin(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/admin/admin-panel#seccion-resenas";
    }

    @DeleteMapping("/book/{id}")
    public String deleteBookAsAdmin(@PathVariable Long id) {
        Book book = bookRepository.findById(id).orElseThrow();
        bookRepository.delete(book);
        return "redirect:/admin/admin-panel#seccion-libros";
    }

    //Edit Loan

    // Load edit view
    @GetMapping("/edit-loans/{id}")
    public String showEditLoanForm(@PathVariable Long id, Model model) {
        Loan loan = loanRepository.findById(id).orElseThrow();
        model.addAttribute("loan", loan);
        return "admin-edit-loans";
    }

    //GEt and save the changes
    @PostMapping("/edit-loans/{id}")
    public String processEditLoan(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam String loanDate,
            @RequestParam String returnDate) {

        Loan existingLoan = loanRepository.findById(id).orElseThrow();

        existingLoan.setStatus(Loan.Status.valueOf(status));

        existingLoan.setLoanDate(java.time.LocalDate.parse(loanDate));
        existingLoan.setReturnDate(java.time.LocalDate.parse(returnDate));

        loanRepository.save(existingLoan);

        return "redirect:/admin/admin-panel#seccion-prestamos";
    }
}