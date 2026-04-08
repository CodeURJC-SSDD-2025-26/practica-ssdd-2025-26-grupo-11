package es.codeurjc.practica2.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import es.codeurjc.practica2.model.Genre;

@Component
public class GenreConverter implements Converter<String, Genre> {
    
    @Override
    public Genre convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        
        try {
            return Genre.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Género inválido: " + source);
        }
    }
}
