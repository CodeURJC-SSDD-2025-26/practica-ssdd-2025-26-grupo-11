package es.codeurjc.practica2.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;   
import es.codeurjc.practica2.model.User;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    
    Optional<User> findByEmail(String email);
}