package br.com.hadryan.app.model.dto;

import java.time.LocalDate;
import java.util.List;

public class BookDTO {

    private String title;

    private String author;

    private LocalDate publishDate;

    private List<PublisherDTO> publishers;

    private List<AuthorDTO> authors;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public List<AuthorDTO> getAuthors() {
        return authors;
    }

    public void setAuthors(List<AuthorDTO> authors) {
        this.authors = authors;
    }

    public List<PublisherDTO> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<PublisherDTO> publishers) {
        this.publishers = publishers;
    }
}
