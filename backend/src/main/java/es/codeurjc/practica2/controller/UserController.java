package es.codeurjc.practica2.controller;
 
import java.io.IOException;
import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
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
import es.codeurjc.practica2.service.BookService;
import es.codeurjc.practica2.service.LoanService;
import es.codeurjc.practica2.service.ReviewService;
import es.codeurjc.practica2.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
 
@Controller
public class UserController {
 
    @Autowired
    private UserService userService;
 
    @Autowired
    private BookService bookService;
 
    @Autowired
    private LoanService loanService;
 
    @Autowired
    private ReviewService reviewService;
 
    @GetMapping("/base")
    public String base(Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        List<Book> recommendations = (user.getLoans() == null || user.getLoans().isEmpty())
                ? null
                : userService.getRecommendedBooks(user);
 
        model.addAttribute("featuredBooks", bookService.findTopRatedBooks());
        model.addAttribute("user", user);
        model.addAttribute("loans", user.getLoans());
        model.addAttribute("name", user.getName());
        model.addAttribute("recommendedBooks", recommendations);
 
        return "base";
    }
 
    @GetMapping("/profile")
    public String profile(Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        populateProfileModel(model, currentUser, true, true);
        return "profile";
    }
 
    @GetMapping("/user/{id}")
    public String viewUserProfile(@PathVariable Long id, Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        User profileUser = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        boolean isAdmin = request.isUserInRole("ADMIN");
        boolean isOwner = currentUser.getId().equals(profileUser.getId());
 
        if (!isAdmin && !isOwner) {
            return "redirect:/base";
        }
 
        populateProfileModel(model, profileUser, isAdmin || isOwner, isOwner);
        return "profile";
    }
 
    @PostMapping("/register")
    public String registerUser(
            Model model,
            @RequestParam String name,
            @RequestParam String surname,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword) throws IOException {
 
        List<String> errors = userService.validateRegistration(name, surname, email, password, confirmPassword);
 
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("name", name);
            model.addAttribute("surname", surname);
            model.addAttribute("email", email);
            return "register";
        }
 
        userService.registerUser(name, surname, email, password);
        model.addAttribute("success", "Usuario registrado correctamente. Ya puedes iniciar sesión.");
        return "login";
    }
 
    @GetMapping("/edit-profile")
    public String editProfile(Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        model.addAttribute("user", user);
        model.addAttribute("editingOwnProfile", true);
        return "edit-profile";
    }
 
    @GetMapping("/edit-profile/{id}")
    public String editProfileById(@PathVariable Long id, Model model, HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();
        User currentUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        User userToEdit = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
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
        User user = userService.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        userService.updateProfile(user.getId(), name, surname, email, bio, imageFile);
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
        User currentUser = userService.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        User userToEdit = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        boolean isAdmin = request.isUserInRole("ADMIN");
        boolean isOwner = currentUser.getId().equals(userToEdit.getId());
 
        if (!isAdmin && !isOwner) {
            return "redirect:/base";
        }
 
        userService.updateProfile(id, name, surname, email, bio, imageFile);
        return "redirect:/user/" + id;
    }
 
    @GetMapping("/users/{id}/image")
    public ResponseEntity<Object> downloadUserImage(@PathVariable long id) throws Exception {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
 
        if (user.getImage() == null) {
            return ResponseEntity.notFound().build();
        }
 
        var imageFile = new InputStreamResource(user.getImage().getImageFile().getBinaryStream());
        MediaType mediaType = MediaTypeFactory.getMediaType(imageFile).orElse(MediaType.IMAGE_JPEG);
 
        return ResponseEntity.ok().contentType(mediaType).body(imageFile);
    }
 
    // -------------------------------------------------------------------------
    // Private helper
    // -------------------------------------------------------------------------
 
    private void populateProfileModel(Model model, User user, boolean canEdit, boolean isCurrentUser) {
        List<Loan> loans = loanService.getLoansForUser(user);
        long activeLoans = loans.stream().filter(Loan::isActive).count();
 
        model.addAttribute("user", user);
        model.addAttribute("totalLoans", loans.size());
        model.addAttribute("totalReviews", reviewService.countByUser(user));
        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("recentLoans", loans);
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("isCurrentUser", isCurrentUser);
    }
}