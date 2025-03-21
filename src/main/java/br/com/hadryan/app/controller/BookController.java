package br.com.hadryan.app.controller;

import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.service.BookService;
import br.com.hadryan.app.service.ServiceFactory;
import br.com.hadryan.app.service.factory.SimilarBookService;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller para operações relacionadas a livros.
 * Implementa o padrão MVC, sendo o intermediário entre a View e o Service.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class BookController {

    private static final Logger LOGGER = Logger.getLogger(BookController.class.getName());

    private final BookService bookService;
    private final SimilarBookService similarBookService;

    /**
     * Construtor padrão que obtém os serviços da Factory
     */
    public BookController() {
        ServiceFactory serviceFactory = ServiceFactory.getInstance();
        this.bookService = serviceFactory.getBookService();
        this.similarBookService = serviceFactory.getSimilarBooksService();
    }

    /**
     * Salva um livro
     *
     * @param book Livro a ser salvo
     * @return Livro salvo com ID gerado
     */
    public Book save(Book book) {
        try {
            return bookService.save(book);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar livro", e);
            throw new RuntimeException("Erro ao salvar livro: " + e.getMessage(), e);
        }
    }

    /**
     * Busca um livro pelo ID
     *
     * @param id ID do livro
     * @return Optional contendo o livro, se encontrado
     */
    public Optional<Book> findById(Long id) {
        return bookService.findById(id);
    }

    /**
     * Busca um livro pelo ISBN
     *
     * @param isbn ISBN do livro
     * @return Optional contendo o livro, se encontrado
     */
    public Optional<Book> findByIsbn(String isbn) {
        return bookService.findByIsbn(isbn);
    }

    /**
     * Lista todos os livros
     *
     * @return Lista de todos os livros
     */
    public List<Book> findAll() {
        return bookService.findAll();
    }

    /**
     * Exclui um livro pelo ID
     *
     * @param id ID do livro a excluir
     */
    public void delete(Long id) {
        try {
            bookService.delete(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir livro com ID: " + id, e);
            throw new RuntimeException("Erro ao excluir livro: " + e.getMessage(), e);
        }
    }

    /**
     * Busca livros que correspondam aos critérios de pesquisa
     *
     * @param criteria Objeto Livro com os critérios de pesquisa
     * @return Lista de livros que correspondem aos critérios
     */
    public List<Book> search(Book criteria) {
        return bookService.search(criteria);
    }

    /**
     * Busca dados do livro pelo ISBN na API do OpenLibrary
     * Se o livro já existir no banco de dados, retorna o existente
     *
     * @param isbn ISBN do livro
     * @return Optional contendo o livro, se encontrado
     */
    public Optional<Book> findBookByIsbnAPI(String isbn) {
        return bookService.findBookByIsbnAPI(isbn);
    }

    /**
     * Atualiza um livro existente com dados da API
     *
     * @param book Livro a atualizar
     * @return Livro atualizado
     */
    public Book updateFromAPI(Book book) {
        return bookService.updateFromAPI(book);
    }

    /**
     * Atualiza os livros similares de um livro
     *
     * @param book Livro a ser atualizado
     * @param similarIsbnList Lista de ISBNs dos livros similares
     * @return Livro atualizado
     */
    public Book updateSimilarBooks(Book book, List<String> similarIsbnList) {
        return similarBookService.updateSimilarBooks(book, similarIsbnList);
    }

    /**
     * Sugere livros similares com base nos livros existentes
     *
     * @param book Livro para sugerir similares
     * @param maxResults Número máximo de resultados
     * @return Lista de livros similares sugeridos
     */
    public List<Book> suggestSimilarBooks(Book book, int maxResults) {
        return similarBookService.suggestSimilarBooks(book, maxResults);
    }

    /**
     * Fecha recursos utilizados pelo controller
     */
    public void close() {
        bookService.close();
    }
}
