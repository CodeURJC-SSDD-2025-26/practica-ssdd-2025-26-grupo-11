package es.codeurjc.practica2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class WebController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("name", "");
        model.addAttribute("surname", "");
        model.addAttribute("email", "");
        return "register";
    }

    @GetMapping("/error/loginerror")
    public String loginError() {
        return "error/loginerror";
    }
}