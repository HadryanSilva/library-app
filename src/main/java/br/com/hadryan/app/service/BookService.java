package br.com.hadryan.app.service;

import br.com.hadryan.app.model.entity.Book;

import java.util.List;
import java.util.Optional;

/**
 * Interface de serviço para operações relacionadas a livros.
 * Implementa o padrão Service para desacoplar a lógica de negócio da camada de acesso a dados.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public interface BookService {

    /**
     * Salva um livro no banco de dados
     *
     * @param book Livro a ser salvo
     * @return Livro com ID gerado
     */
    Book save(Book book);

    /**
     * Busca um livro pelo ID
     *
     * @param id ID do livro a ser buscado
     * @return Optional contendo o livro, se encontrado
     */
    Optional<Book> findById(Long id);

    /**
     * Busca um livro pelo ISBN
     *
     * @param isbn ISBN do livro a ser buscado
     * @return Optional contendo o livro, se encontrado
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Lista todos os livros
     *
     * @return Lista de todos os livros
     */
    List<Book> findAll();

    /**
     * Exclui um livro pelo ID
     *
     * @param id ID do livro a ser excluído
     */
    void delete(Long id);

    /**
     * Busca livros que correspondam aos critérios de pesquisa
     *
     * @param criteria Objeto Livro com os critérios de pesquisa
     * @return Lista de livros que correspondem aos critérios
     */
    List<Book> search(Book criteria);

    /**
     * Busca informações do livro pelo ISBN na API do OpenLibrary
     *
     * @param isbn ISBN do livro a ser buscado
     * @return Optional contendo o livro, se encontrado
     */
    Optional<Book> findBookByIsbnAPI(String isbn);

    /**
     * Atualiza um livro existente com dados da API do OpenLibrary
     *
     * @param book Livro a ser atualizado
     * @return Livro atualizado
     */
    Book updateFromAPI(Book book);

    /**
     * Fecha os recursos utilizados pelo serviço
     */
    void close();
}