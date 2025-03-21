package br.com.hadryan.app.controller;

import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.model.repository.BookRepository;
import br.com.hadryan.app.service.OpenLibraryService;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookController {

    private static final Logger LOGGER = Logger.getLogger(BookController.class.getName());

    private final BookRepository bookRepository;
    private final OpenLibraryService openLibraryService;

    public BookController() {
        bookRepository = new BookRepository();
        openLibraryService = new OpenLibraryService();
    }

    /**
     * Saves a book to the database
     *
     * @param book Book to be saved
     * @return Saved book with generated ID
     */
    public Book save(Book book) {
        try {
            return bookRepository.save(book);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving book", e);
            throw new RuntimeException("Error saving book: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a book by ID
     *
     * @param id ID of the book
     * @return Optional containing the book, if found
     */
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    /**
     * Finds a book by ISBN
     *
     * @param isbn ISBN of the book
     * @return Optional containing the book, if found
     */
    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    /**
     * Lists all books
     *
     * @return List of all books
     */
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    /**
     * Deletes a book by ID
     *
     * @param id ID of the book to delete
     */
    public void delete(Long id) {
        try {
            bookRepository.delete(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting book with ID: " + id, e);
            throw new RuntimeException("Error deleting book: " + e.getMessage(), e);
        }
    }

    /**
     * Searches for books based on criteria
     *
     * @param book Book with search criteria
     * @return List of books matching the criteria
     */
    public List<Book> search(Book book) {
        return bookRepository.search(book);
    }

    /**
     * Finds book data by ISBN from the OpenLibrary API
     * If the book already exists in the database, returns the existing one
     *
     * @param isbn ISBN of the book
     * @return Optional containing the book, if found
     */
    public Optional<Book> findBookByIsbnAPI(String isbn) {
        // First check if the book already exists in the database
        Optional<Book> existingBook = bookRepository.findByIsbn(isbn);
        if (existingBook.isPresent()) {
            return existingBook;
        }

        // If not found in the database, try to fetch from the API
        return openLibraryService.findBookByIsbn(isbn);
    }

    /**
     * Updates an existing book with data from the API
     *
     * @param book Book to update
     * @return Updated book
     */
    public Book updateFromAPI(Book book) {
        if (book.getIsbn() == null || book.getIsbn().isEmpty()) {
            throw new IllegalArgumentException("ISBN is required for API lookup");
        }

        Optional<Book> apiBook = openLibraryService.findBookByIsbn(book.getIsbn());
        if (apiBook.isPresent()) {
            Book updatedBook = apiBook.get();
            // Preserve the ID of the original book
            updatedBook.setId(book.getId());
            return bookRepository.save(updatedBook);
        }

        return book;
    }

    /**
     * Closes resources used by the controller
     */
    public void close() {
        bookRepository.close();
    }

}
