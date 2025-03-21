package br.com.hadryan.app.service;

import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.model.entity.Publisher;
import br.com.hadryan.app.model.repository.AuthorRepository;
import br.com.hadryan.app.model.repository.BookRepository;
import br.com.hadryan.app.model.repository.PublisherRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportService {

    private static final Logger LOGGER = Logger.getLogger(ImportService.class.getName());

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;

    public ImportService() {
        bookRepository = new BookRepository();
        authorRepository = new AuthorRepository();
        publisherRepository = new PublisherRepository();
    }

    /**
     * Imports books from a file
     *
     * @param file File to import
     * @return Number of books imported
     * @throws IOException if an I/O error occurs
     */
    public int importBooks(File file) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".csv")) {
            return importFromCSV(file);
        } else if (fileName.endsWith(".xml")) {
            return importFromXML(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + fileName);
        }
    }

    /**
     * Imports books from a CSV file
     *
     * @param file CSV file to import
     * @return Number of books imported
     * @throws IOException if an I/O error occurs
     */
    private int importFromCSV(File file) throws IOException {
        List<Book> importedBooks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Header line
            String[] headers = line.split(",");

            // Map column indices
            int titleIndex = -1;
            int isbnIndex = -1;
            int authorIndex = -1;
            int publisherIndex = -1;
            int pubDateIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim().toLowerCase();
                if (header.equals("title")) {
                    titleIndex = i;
                } else if (header.equals("isbn")) {
                    isbnIndex = i;
                } else if (header.equals("author") || header.equals("authors")) {
                    authorIndex = i;
                } else if (header.equals("publisher")) {
                    publisherIndex = i;
                } else if (header.contains("date")) {
                    pubDateIndex = i;
                }
            }

            // Read data lines
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                if (values.length >= Math.max(Math.max(titleIndex, isbnIndex),
                        Math.max(authorIndex, Math.max(publisherIndex, pubDateIndex)))) {
                    Book book = new Book();

                    if (titleIndex >= 0) {
                        book.setTitle(values[titleIndex].trim());
                    }

                    if (isbnIndex >= 0) {
                        book.setIsbn(values[isbnIndex].trim());
                    }

                    if (pubDateIndex >= 0) {
                        LocalDate pubDate = LocalDate.parse(values[pubDateIndex].trim());
                        book.setPublicationDate(pubDate);
                    }

                    if (publisherIndex >= 0 && !values[publisherIndex].trim().isEmpty()) {
                        String publisherName = values[publisherIndex].trim();
                        Publisher publisher = publisherRepository.findByName(publisherName)
                                .orElse(new Publisher(publisherName));
                        book.setPublisher(publisher);
                    }

                    if (authorIndex >= 0 && !values[authorIndex].trim().isEmpty()) {
                        String[] authorNames = values[authorIndex].trim().split(";");
                        for (String authorName : authorNames) {
                            Author author = authorRepository.findByName(authorName.trim())
                                    .orElse(new Author(authorName.trim()));
                            book.addAuthor(author);
                        }
                    }

                    importedBooks.add(book);
                }
            }
        }

        return saveImportedBooks(importedBooks);
    }

    /**
     * Imports books from an XML file
     *
     * @param file XML file to import
     * @return Number of books imported
     */
    private int importFromXML(File file) {
        List<Book> importedBooks = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            NodeList bookNodes = document.getElementsByTagName("book");
            for (int i = 0; i < bookNodes.getLength(); i++) {
                Element bookElement = (Element) bookNodes.item(i);
                Book book = new Book();

                // Title
                NodeList titleNodes = bookElement.getElementsByTagName("title");
                if (titleNodes.getLength() > 0) {
                    book.setTitle(titleNodes.item(0).getTextContent());
                }

                // ISBN
                NodeList isbnNodes = bookElement.getElementsByTagName("isbn");
                if (isbnNodes.getLength() > 0) {
                    book.setIsbn(isbnNodes.item(0).getTextContent());
                }

                // Publication Date
                NodeList dateNodes = bookElement.getElementsByTagName("publicationDate");
                if (dateNodes.getLength() > 0) {
                    LocalDate pubDate = LocalDate.parse(dateNodes.item(0).getTextContent());
                    book.setPublicationDate(pubDate);
                }

                // Publisher
                NodeList publisherNodes = bookElement.getElementsByTagName("publisher");
                if (publisherNodes.getLength() > 0) {
                    String publisherName = publisherNodes.item(0).getTextContent();
                    Publisher publisher = publisherRepository.findByName(publisherName)
                            .orElse(new Publisher(publisherName));
                    book.setPublisher(publisher);
                }

                // Authors
                NodeList authorNodes = bookElement.getElementsByTagName("author");
                for (int j = 0; j < authorNodes.getLength(); j++) {
                    String authorName = authorNodes.item(j).getTextContent();
                    Author author = authorRepository.findByName(authorName)
                            .orElse(new Author(authorName));
                    book.addAuthor(author);
                }

                importedBooks.add(book);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error importing from XML", e);
            throw new RuntimeException("Error importing from XML: " + e.getMessage(), e);
        }

        return saveImportedBooks(importedBooks);
    }

    /**
     * Saves imported books to the database
     * Updates existing books if ISBN already exists
     *
     * @param books List of books to save
     * @return Number of books saved
     */
    private int saveImportedBooks(List<Book> books) {
        int count = 0;

        for (Book book : books) {
            try {
                // Skip books without ISBN
                if (book.getIsbn() == null || book.getIsbn().isEmpty()) {
                    continue;
                }

                // Check if the book already exists
                Optional<Book> existingBook = bookRepository.findByIsbn(book.getIsbn());

                if (existingBook.isPresent()) {
                    // Update existing book
                    Book existing = existingBook.get();

                    // Update fields if provided in the import
                    if (book.getTitle() != null && !book.getTitle().isEmpty()) {
                        existing.setTitle(book.getTitle());
                    }

                    if (book.getPublicationDate() != null) {
                        existing.setPublicationDate(book.getPublicationDate());
                    }

                    if (book.getPublisher() != null) {
                        existing.setPublisher(book.getPublisher());
                    }

                    // Add new authors
                    for (Author author : book.getAuthors()) {
                        existing.addAuthor(author);
                    }

                    bookRepository.save(existing);
                } else {
                    // Save new book
                    bookRepository.save(book);
                }

                count++;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error saving imported book: " + book.getIsbn(), e);
            }
        }

        return count;
    }

    /**
     * Closes resources used by the service
     */
    public void close() {
        bookRepository.close();
        authorRepository.close();
        publisherRepository.close();
    }

}
