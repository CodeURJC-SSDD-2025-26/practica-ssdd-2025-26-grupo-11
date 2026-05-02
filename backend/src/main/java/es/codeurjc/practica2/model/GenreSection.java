package es.codeurjc.practica2.model;

import java.util.List;

public class GenreSection {

    private String genreName;
    private String genreCode;
    private List<Book> books;
    private int currentPage;      
    private boolean hasNextPage;
    private int totalPages;

    public GenreSection(String genreName, String genreCode, List<Book> books,
                        int currentPage, boolean hasNextPage, int totalPages) {
        this.genreName = genreName;
        this.genreCode = genreCode;
        this.books = books;
        this.currentPage = currentPage;
        this.hasNextPage = hasNextPage;
        this.totalPages = totalPages;
    }

    public GenreSection(String genreName, String genreCode, List<Book> books) {
        this(genreName, genreCode, books, 0, false, 1);
    }

    public String getGenreName()  { return genreName; }
    public String getGenreCode()  { return genreCode; }
    public List<Book> getBooks()  { return books; }
    public boolean isHasNextPage() { return hasNextPage; }
    public int getTotalPages()    { return totalPages; }


    public int getDisplayPage()   { return currentPage + 1; }
    public int getNextPage()      { return currentPage + 1; } 
    public int getPrevPage()      { return currentPage - 1; }  
    public boolean isFirstPage()  { return currentPage == 0; }
    public boolean isLastPage()   { return !hasNextPage; }
}