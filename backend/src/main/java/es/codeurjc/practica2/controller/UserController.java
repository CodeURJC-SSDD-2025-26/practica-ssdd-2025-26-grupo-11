package es.codeurjc.practica2.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
import es.codeurjc.practica2.model.Image;
import es.codeurjc.practica2.model.Loan;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.LoanRepository;
import es.codeurjc.practica2.repository.ReviewRepository;
import es.codeurjc.practica2.repository.UserRepository;
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.ImageService;
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

    @Autowired
    private ImageService imageService;

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
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Loan> recentLoans = loanRepository.findByUser(currentUser);
        for (Loan loan : recentLoans) {
            if (loan.getStatus() != Loan.Status.DEVUELTO) {
                loan.refreshStatusFromDates();
            }
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("totalLoans", recentLoans.size());
        model.addAttribute("totalReviews", reviewRepository.findByUser(currentUser).size());
        model.addAttribute("activeLoans", recentLoans.stream().filter(Loan::isActive).count());
        model.addAttribute("recentLoans", recentLoans);
        model.addAttribute("canEdit", true);
        model.addAttribute("isCurrentUser", true);

        return "profile";
    }

    @GetMapping("/user/{id}")
    public String viewUserProfile(@PathVariable Long id, Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        User profileUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Check permissions: only admin or profile owner
        boolean isAdmin = request.isUserInRole("ADMIN");
        boolean isOwner = currentUser.getId().equals(profileUser.getId());

        if (!isAdmin && !isOwner) {
            return "redirect:/base";
        }

        List<Loan> recentLoans = loanRepository.findByUser(profileUser);
        for (Loan loan : recentLoans) {
            if (loan.getStatus() != Loan.Status.DEVUELTO) {
                loan.refreshStatusFromDates();
            }
        }

        model.addAttribute("user", profileUser);
        model.addAttribute("totalLoans", recentLoans.size());
        model.addAttribute("totalReviews", reviewRepository.findByUser(profileUser).size());
        model.addAttribute("activeLoans", recentLoans.stream().filter(Loan::isActive).count());
        model.addAttribute("recentLoans", recentLoans);
        model.addAttribute("canEdit", isAdmin || isOwner);
        model.addAttribute("isCurrentUser", isOwner);

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

        List<String> errors = new ArrayList<>();

        if (name == null || name.isBlank()) {
            errors.add("El nombre es obligatorio.");
        } else if (name.length() > 22) {
            errors.add("El nombre no puede superar los 22 caracteres.");
        }

        if (surname == null || surname.isBlank()) {
            errors.add("El apellido es obligatorio.");
        } else if (surname.length() > 22) {
            errors.add("El apellido no puede superar los 22 caracteres.");
        }

        if (email == null || email.isBlank()) {
            errors.add("El email es obligatorio.");
        } else if (email.length() > 30) {
            errors.add("El email no puede superar los 30 caracteres.");
        } else if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.add("El formato del email no es válido.");
        }

        if (password == null || password.isBlank()) {
            errors.add("La contraseña es obligatoria.");
        } else if (password.length() < 6) {
            errors.add("La contraseña debe tener al menos 6 caracteres.");
        } else if (password.length() > 30) {
            errors.add("La contraseña no puede superar los 30 caracteres.");
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            errors.add("Debes confirmar la contraseña.");
        } else if (!password.equals(confirmPassword)) {
            errors.add("Las contraseñas no coinciden.");
        }

        if (email != null && !email.isBlank() && userService.emailExists(email)) {
            errors.add("Este correo ya está registrado.");
        }

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("name", name);
            model.addAttribute("surname", surname);
            model.addAttribute("email", email);
            return "register";
        }

        User newUser = userService.registerUser(name, surname, email, password);

        try {
            Resource resource = new ClassPathResource("static/images/default-avatar.png");
            if (resource.exists()) {
                Image image = imageService.createImage(resource.getInputStream());
                newUser.setImage(image);
                userRepository.save(newUser);
            }
        } catch (IOException e) {
        }

        model.addAttribute("success", "Usuario registrado correctamente. Ya puedes iniciar sesión.");
        return "login";
    }

    @GetMapping("/edit-profile")
    public String editProfile(Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("user", user);
        model.addAttribute("editingOwnProfile", true);

        return "edit-profile";
    }

    @GetMapping("/edit-profile/{id}")
    public String editProfileById(@PathVariable Long id, Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        User userToEdit = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Check permissions: only admin or profile owner
        boolean isAdmin = request.isUserInRole("ADMIN");
        boolean isOwner = currentUser.getId().equals(userToEdit.getId());

        if (!isAdmin && !isOwner) {
            return "redirect:/base";
        }

        model.addAttribute("user", userToEdit);
        model.addAttribute("editingOwnProfile", isOwner);

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
            if (user.getImage() != null) {
                imageService.replaceImageFile(user.getImage().getId(), imageFile.getInputStream());
            } else {
                Image image = imageService.createImage(imageFile.getInputStream());
                user.setImage(image);
            }
        }

        userRepository.save(user);

        return "redirect:/profile";
    }

    @PostMapping("/edit-profile/{id}")
    public String updateProfileById(
            @PathVariable Long id,
            HttpServletRequest request,
            @RequestParam String name,
            @RequestParam String surname,
            @RequestParam String email,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) MultipartFile imageFile) throws IOException {

        String currentEmail = request.getUserPrincipal().getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        User userToEdit = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Check permissions: only admin or profile owner
        boolean isAdmin = request.isUserInRole("ADMIN");
        boolean isOwner = currentUser.getId().equals(userToEdit.getId());

        if (!isAdmin && !isOwner) {
            return "redirect:/base";
        }

        userToEdit.setName(name);
        userToEdit.setSurname(surname);
        userToEdit.setEmail(email);
        userToEdit.setDescription(bio);

        if (imageFile != null && !imageFile.isEmpty()) {
            if (userToEdit.getImage() != null) {
                imageService.replaceImageFile(userToEdit.getImage().getId(), imageFile.getInputStream());
            } else {
                Image image = imageService.createImage(imageFile.getInputStream());
                userToEdit.setImage(image);
            }
        }

        userRepository.save(userToEdit);

        return "redirect:/user/" + id;
    }

    @GetMapping("/users/{id}/image")
    public ResponseEntity<Object> downloadUserImage(@PathVariable long id) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getImage() != null) {
            Resource imageFile = new InputStreamResource(user.getImage().getImageFile().getBinaryStream());

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

}
