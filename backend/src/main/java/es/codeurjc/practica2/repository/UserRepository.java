package es.codeurjc.practica2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.codeurjc.practica2.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    /**
     * Admin panel: search users by name, surname or email.
     * Null query returns all users.
     */
    @Query("""
            SELECT u FROM User u
            WHERE (
                :q IS NULL
                OR LOWER(u.name)    LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.email)   LIKE LOWER(CONCAT('%', :q, '%'))
            )
            ORDER BY u.id ASC
            """)
    List<User> searchUsers(@Param("q") String q);
}