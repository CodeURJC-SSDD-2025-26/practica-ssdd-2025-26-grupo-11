package es.codeurjc.practica2.model;

import java.sql.Blob;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String author;

    private String description;

    private String genre;

    private float rating;
    private int year;
    private long isbn;

    @OneToOne
    private Image image;

    // A book can have many reviews, but a review belongs to one book
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    public Book() {
    }

    public Book(String title, String author, String description, String genre, float rating, int year, long isbn) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.genre = genre;
        this.rating = rating;
        this.year = year;
        this.isbn = isbn;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getIsbn() {
        return isbn;
    }

    public void setIsbn(long isbn) {
        this.isbn = isbn;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void addReview(Review review) {
        reviews.add(review);
        review.setBook(this);
    }
}