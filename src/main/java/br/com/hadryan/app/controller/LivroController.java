package br.com.hadryan.app.controller;

import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.service.LivroService;

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
public class LivroController {

    private static final Logger LOGGER = Logger.getLogger(LivroController.class.getName());

    private final LivroService livroService;

    /**
     * Construtor que recebe o serviço de livros
     */
    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    /**
     * Salva um livro
     */
    public Livro salvar(Livro livro) {
        try {
            return livroService.salvar(livro);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar livro", e);
            throw new RuntimeException("Erro ao salvar livro: " + e.getMessage(), e);
        }
    }

    /**
     * Busca um livro pelo ID
     */
    public Optional<Livro> buscarPorId(Long id) {
        return livroService.buscarPorId(id);
    }

    /**
     * Busca um livro pelo ISBN
     */
    public Optional<Livro> buscarPorIsbn(String isbn) {
        return livroService.buscarPorIsbn(isbn);
    }

    /**
     * Lista todos os livros
     */
    public List<Livro> listarTodos() {
        return livroService.listarTodos();
    }

    /**
     * Exclui um livro pelo ID
     */
    public void excluir(Long id) {
        try {
            livroService.excluir(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir livro com ID: " + id, e);
            throw new RuntimeException("Erro ao excluir livro: " + e.getMessage(), e);
        }
    }

    /**
     * Pesquisa livros com base em critérios
     */
    public List<Livro> pesquisar(Livro filtro) {
        return livroService.pesquisar(filtro);
    }

    /**
     * Busca livro pelo ISBN na API
     */
    public Optional<Livro> buscarLivroPorIsbnApi(String isbn) {
        try {
            return livroService.buscarLivroPorIsbnApi(isbn);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar livro por ISBN na API: " + isbn, e);
            throw new RuntimeException("Erro na consulta da API: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza os livros similares de um livro
     */
    public void atualizarLivrosSimilares(Livro livro, List<String> listaIsbn) {
        try {
            livroService.atualizarLivrosSimilares(livro, listaIsbn);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar livros similares", e);
            throw new RuntimeException("Erro ao atualizar livros similares: " + e.getMessage(), e);
        }
    }

    /**
     * Sugere livros similares
     */
    public List<Livro> sugerirLivrosSimilares(Livro livro, int maxResultados) {
        return livroService.sugerirLivrosSimilares(livro, maxResultados);
    }
}
