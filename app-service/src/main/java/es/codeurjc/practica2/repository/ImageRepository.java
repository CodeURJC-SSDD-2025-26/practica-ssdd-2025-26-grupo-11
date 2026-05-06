package es.codeurjc.practica2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.practica2.model.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
