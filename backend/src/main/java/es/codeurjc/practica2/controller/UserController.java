package es.codeurjc.practica2.controller;

import java.io.IOException;
import java.util.List;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.practica2.model.Book;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.LoanRepository;
import es.codeurjc.practica2.repository.ReviewRepository;
import es.codeurjc.practica2.repository.UserRepository;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @GetMapping("/base")
    public String base(Model model, HttpServletRequest request) {

        String email = request.getUserPrincipal().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("featuredBooks", bookService.findTopRatedBooks());
        model.addAttribute("user", user);
        model.addAttribute("loans", user.getLoans());
        model.addAttribute("name", user.getName());

        if (user.getLoans() == null || user.getLoans().isEmpty()) {
            model.addAttribute("recommendedBooks", null);
        } else {
            List<Book> recommendations = userService.getRecommendedBooks(user);
            model.addAttribute("recommendedBooks", recommendations);
        }

        return "base";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Loan> recentLoans = loanRepository.findByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("totalLoans", recentLoans.size());
        model.addAttribute("totalReviews", reviewRepository.findByUser(user).size());
        model.addAttribute("activeLoans",recentLoans.stream().filter(loan -> loan.getStatus() == Loan.Status.ACTIVO).count());
        model.addAttribute("recentLoans", recentLoans);

        return "profile";
    }

    @PostMapping("/register")
    public String registerUser(
            Model model,
            @RequestParam String name,
            @RequestParam String surname,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword) {

        model.addAttribute("name", name);
        model.addAttribute("surname", surname);
        model.addAttribute("email", email);

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "register";
        }

        if (userService.emailExists(email)) {
            model.addAttribute("error", "Este correo ya está registrado");
            return "register";
        }

        userService.registerUser(name, surname, email, password);

        model.addAttribute("success", "Usuario registrado correctamente. Ya puedes iniciar sesión.");
        return "login";
    }

    @GetMapping("/edit-profile")
    public String editProfile(Model model, HttpServletRequest request) {

        String email = request.getUserPrincipal().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("user", user);

        return "edit-profile";
    }

    @PostMapping("/edit-profile")
    public String updateProfile(
            HttpServletRequest request,
            @RequestParam String name,
            @RequestParam String surname,
            @RequestParam String email,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) MultipartFile imageFile) throws IOException {

        String currentEmail = request.getUserPrincipal().getName();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setDescription(bio);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                user.setImageFile(new SerialBlob(imageFile.getBytes()));
            } catch (Exception e) {
                throw new IOException("Error al guardar la imagen de perfil", e);
            }
        }

        userRepository.save(user);

        return "redirect:/profile";
    }

    @GetMapping("/users/{id}/image")
    public ResponseEntity<Object> downloadUserImage(@PathVariable long id) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getImageFile() != null) {
            Resource imageFile = new InputStreamResource(user.getImageFile().getBinaryStream());

            MediaType mediaType = MediaTypeFactory
                    .getMediaType(imageFile)
                    .orElse(MediaType.IMAGE_JPEG);

            return ResponseEntity
                    .ok()
                    .contentType(mediaType)
                    .body(imageFile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my-loans")
    public String myLoans(Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Loan> loans = loanRepository.findByUser(user);

        long activeLoans = loans.stream()
                .filter(loan -> loan.getStatus() == Loan.Status.ACTIVO)
                .count();

        long overdueLoans = loans.stream()
                .filter(loan -> loan.getStatus() == Loan.Status.VENCIDO)
                .count();

        long returnedLoans = loans.stream()
                .filter(loan -> loan.getStatus() == Loan.Status.DEVUELTO)
                .count();

        model.addAttribute("user", user);
        model.addAttribute("loans", loans);
        model.addAttribute("activeLoansCount", activeLoans);
        model.addAttribute("overdueLoansCount", overdueLoans);
        model.addAttribute("returnedLoansCount", returnedLoans);
        model.addAttribute("totalLoansCount", loans.size());

        return "my-loans";
    }

    @PostMapping("/my-loans/return/{id}")
    public String returnLoan(@PathVariable Long id, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (!loan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permisos para modificar este préstamo");
        }

        loan.setStatus(Loan.Status.DEVUELTO);
        loanRepository.save(loan);

        return "redirect:/my-loans";
    }
}
