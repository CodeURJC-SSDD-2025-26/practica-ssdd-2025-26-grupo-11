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

    @GetMapping("/login")
    public String login() {
        return "login";  
    }
    
    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }
}