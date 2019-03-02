package edu.uci.ics.fabflixmobile;

public class Movie {
    private String name;
    private Integer castYear;
    private String director;
    private String genres;
    private String stars;

    public Movie(String name, int castYear, String director, String genres, String stars) {
        this.name = name;
        this.castYear = castYear;
        this.director = director;
        this.genres = genres;
        this.stars = stars;
    }

    public String getName() {
        return name;
    }
    public Integer getCastYear() {
        return castYear;
    }
    public String getdirector() {
        return director;
    }
    public String getgenres() {
        return genres;
    }
    public String getstars() {
        return stars;
    }

}
