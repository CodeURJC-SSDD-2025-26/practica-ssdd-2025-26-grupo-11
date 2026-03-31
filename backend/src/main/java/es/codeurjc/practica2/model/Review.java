package es.codeurjc.practica2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1500)
    private String comment;

    private Integer rating;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Book book;

    public Review() {
    }

    public Review(String comment, int rating, User user, Book book) {
        this.comment = comment;
        this.rating = rating;
        this.user = user;
        this.book = book;
    }

    public Long getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public Integer getRating() {
        return rating;
    }

    public User getUser() {
        return user;
    }

    public Book getBook() {
        return book;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}