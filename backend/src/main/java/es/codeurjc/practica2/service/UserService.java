package es.codeurjc.practica2.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User registerUser(String name, String surname, String email, String rawPassword) {
        User user = new User(
                name,
                surname,
                passwordEncoder.encode(rawPassword),
                email,
                new Date(),
                "USER"
        );

        return userRepository.save(user);
    }
}