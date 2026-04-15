package es.codeurjc.practica2.model;

import java.util.List;

public class GenreSection {

    private String genreName;
    private String genreCode;
    private List<Book> books;

    public GenreSection(String genreName, String genreCode, List<Book> books) {
        this.genreName = genreName;
        this.genreCode = genreCode;
        this.books = books;
    }

    public String getGenreName() {
        return genreName;
    }

    public String getGenreCode() {
        return genreCode;
    }

    public List<Book> getBooks() {
        return books;
    }
}