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
import jakarta.servlet.http.HttpServletRequest;
import es.codeurjc.practica2.model.User;

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

    @PostMapping("/user/{id}/delete")
    public String deleteUserAsAdmin(@PathVariable Long id, HttpServletRequest request) {
        User userToDelete = userRepository.findById(id).orElseThrow();

        //The admin cannot delete himself or herself
        String loggedInEmail = request.getUserPrincipal().getName();
        if (userToDelete.getEmail().equals(loggedInEmail)) {
            return "redirect:/admin/admin-panel#seccion-usuarios";
        }

        //Control the deletion of the image with the user
        if (userToDelete.getImage() != null) {
            Long imageId = userToDelete.getImage().getId();
            userToDelete.setImage(null);
            userRepository.save(userToDelete); 
            imageRepository.deleteById(imageId);
        }

        userRepository.delete(userToDelete);

        return "redirect:/admin/admin-panel#seccion-usuarios";
    }
    @PostMapping("/loan/{id}/delete")
    public String deleteLoanAsAdmin(@PathVariable Long id) {
        loanRepository.deleteById(id);
        return "redirect:/admin/admin-panel#seccion-prestamos";
    }
}