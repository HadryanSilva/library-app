package br.com.hadryan.app.service.impl;

import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.model.repository.BookRepository;
import br.com.hadryan.app.model.repository.RepositoryFactory;
import br.com.hadryan.app.service.BookService;
import br.com.hadryan.app.service.OpenLibraryService;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementação do serviço de livros.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class BookServiceImpl implements BookService {

    private static final Logger LOGGER = Logger.getLogger(BookServiceImpl.class.getName());

    private final BookRepository bookRepository;
    private final OpenLibraryService openLibraryService;

    public BookServiceImpl() {
        this.bookRepository = RepositoryFactory.getInstance().getBookRepository();
        this.openLibraryService = new OpenLibraryService();
    }

    @Override
    public Book save(Book book) {
        try {
            return bookRepository.save(book);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar livro", e);
            throw new RuntimeException("Erro ao salvar livro: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        try {
            bookRepository.delete(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir livro com ID: " + id, e);
            throw new RuntimeException("Erro ao excluir livro: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Book> search(Book criteria) {
        return bookRepository.search(criteria);
    }

    @Override
    public Optional<Book> findBookByIsbnAPI(String isbn) {
        // Primeiro verifica se o livro já existe no banco de dados
        Optional<Book> existingBook = bookRepository.findByIsbn(isbn);
        if (existingBook.isPresent()) {
            return existingBook;
        }

        // Se não encontrado no banco de dados, tenta buscar na API
        return openLibraryService.findBookByIsbn(isbn);
    }

    @Override
    public Book updateFromAPI(Book book) {
        if (book.getIsbn() == null || book.getIsbn().isEmpty()) {
            throw new IllegalArgumentException("ISBN é obrigatório para busca na API");
        }

        Optional<Book> apiBook = openLibraryService.findBookByIsbn(book.getIsbn());
        if (apiBook.isPresent()) {
            Book updatedBook = apiBook.get();
            // Preserva o ID do livro original
            updatedBook.setId(book.getId());
            // Preserva livros similares
            updatedBook.setSimilarBooks(book.getSimilarBooks());
            return bookRepository.save(updatedBook);
        }

        return book;
    }

    @Override
    public void close() {
        bookRepository.close();
    }
}