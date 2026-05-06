package es.codeurjc.practica2.model;

public enum Genre {
    FICCION("Ficción"),
    FANTASIA("Fantasía"),
    HISTORIA("Historia"),
    CIENCIA("Ciencia"),
    INFANTIL("Infantil"),
    MISTERIO("Misterio"),
    ROMANCE("Romance"),
    BIOGRAFIA("Biografía"),
    CLASICOS("Clásicos");

    private final String displayName;

    Genre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
