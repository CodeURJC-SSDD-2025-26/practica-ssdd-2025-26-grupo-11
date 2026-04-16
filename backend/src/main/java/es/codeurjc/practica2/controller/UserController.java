package es.codeurjc.practica2.controller;

import java.io.IOException;
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

        User newUser = userService.registerUser(name, surname, email, password);

        // Assign default profile image
        try {
            Resource resource = new ClassPathResource("static/images/default-avatar.png");
            if (resource.exists()) {
                Image image = imageService.createImage(resource.getInputStream());
                newUser.setImage(image);
                userRepository.save(newUser);
            }
        } catch (IOException e) {
            // If image cannot be loaded, continue without it
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

    @GetMapping("/my-loans")
    public String myLoans(Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Loan> loans = loanRepository.findByUser(user);
        for (Loan loan : loans) {
            if (loan.getStatus() != Loan.Status.DEVUELTO) {
                loan.refreshStatusFromDates();
            }
}

        long activeLoans = loans.stream()
                .filter(Loan::isActive)
                .count();

        long overdueLoans = loans.stream()
                .filter(Loan::isOverdue)
                .count();

        long returnedLoans = loans.stream()
                .filter(Loan::isReturned)
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
        loan.setReturnDate(java.time.LocalDate.now());
        loanRepository.save(loan);

        return "redirect:/my-loans";
    }

    @GetMapping("/admin/user/{id}")
    public String viewUserFromAdmin(@PathVariable Long id) {
        return "redirect:/user/" + id;
    }
}
