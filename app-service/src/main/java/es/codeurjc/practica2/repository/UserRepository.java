package es.codeurjc.practica2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.codeurjc.practica2.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

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

    /**
     * Pageable version used by the admin panel.
     */
    @Query(value = """
            SELECT u FROM User u
            WHERE (
                :q IS NULL
                OR LOWER(u.name)    LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.email)   LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """,
            countQuery = """
            SELECT COUNT(u) FROM User u
            WHERE (
                :q IS NULL
                OR LOWER(u.name)    LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.surname) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(u.email)   LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """)
    Page<User> searchUsersPage(@Param("q") String q, Pageable pageable);
}