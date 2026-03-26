package es.codeurjc.practica2.security;

import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void addUserInfoToModel(Model model, HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            String email = request.getUserPrincipal().getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                model.addAttribute("logged", true);
                model.addAttribute("userName", user.getName());
                model.addAttribute("id", user.getId());
                model.addAttribute("admin", user.getRoles().contains("ADMIN"));
                return;
            }
        }
        model.addAttribute("logged", false);
    }
}