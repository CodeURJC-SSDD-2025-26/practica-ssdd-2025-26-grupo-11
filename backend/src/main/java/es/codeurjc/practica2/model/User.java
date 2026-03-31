package es.codeurjc.practica2.model;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private String encodedPassword;
    private String description;

    private String email;
    private Date registrationDate;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    @Lob
    private Blob imageFile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();


    public User(String name, String surname,String encodedPassword, String email, Date registrationDate, String... roles) {
        this.name = name;
        this.surname = surname;
        this.encodedPassword = encodedPassword;
        this.email = email;
        this.registrationDate = registrationDate;
        this.roles = List.of(roles);
        this.description = "Añade una descripción a tu perfil.";
        this.loans = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }
    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Blob getImageFile() {
        return imageFile;
    }

    public void setImageFile(Blob imageFile) {
        this.imageFile = imageFile;
    }

    public List<Loan> getLoans() {
        return loans;
    }

    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public boolean addLoan(Loan loan){
        if (loans.contains(loan)) {
            return false;
        }
        loans.add(loan);
        return true;
    }

      public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }



    public void addReview(Review review) {
    reviews.add(review);
    review.setUser(this);
}
}
