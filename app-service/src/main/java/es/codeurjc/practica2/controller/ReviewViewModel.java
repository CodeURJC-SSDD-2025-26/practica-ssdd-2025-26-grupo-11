package es.codeurjc.practica2.controller;
import es.codeurjc.practica2.model.Review;
import es.codeurjc.practica2.model.User;

public class ReviewViewModel {
    private final Review review;
    private final boolean canDeleteReview;

    public ReviewViewModel(Review review, boolean canDeleteReview) {
        this.review = review;
        this.canDeleteReview = canDeleteReview;
    }

    public Long getId() { return review.getId(); }
    public String getComment() { return review.getComment(); }
    public Integer getRating() { return review.getRating(); }
    public User getUser() { return review.getUser(); }
    public boolean isCanDeleteReview() { return canDeleteReview; }
}
