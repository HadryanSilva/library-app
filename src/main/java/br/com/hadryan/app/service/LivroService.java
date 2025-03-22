package br.com.hadryan.app.service;

import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.model.repository.LivroRepository;
import br.com.hadryan.app.service.api.OpenLibraryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço responsável pela lógica de negócio relacionada a livros.
 * Implementa o padrão Service Layer.
 */
public class LivroService {

    private static final Logger LOGGER = Logger.getLogger(LivroService.class.getName());

    private final LivroRepository livroRepository;
    private final OpenLibraryService openLibraryService;

    /**
     * Construtor com injeção de dependências
     */
    public LivroService(LivroRepository livroRepository, OpenLibraryService openLibraryService) {
        this.livroRepository = livroRepository;
        this.openLibraryService = openLibraryService;
    }

    /**
     * Salva um livro
     */
    public Livro salvar(Livro livro) {
        try {
            return livroRepository.save(livro);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar livro", e);
            throw new RuntimeException("Erro ao salvar livro: " + e.getMessage(), e);
        }
    }

    /**
     * Busca um livro pelo ID
     */
    public Optional<Livro> buscarPorId(Long id) {
        return livroRepository.findById(id);
    }

    /**
     * Busca um livro pelo ISBN
     */
    public Optional<Livro> buscarPorIsbn(String isbn) {
        return livroRepository.findByIsbn(isbn);
    }

    /**
     * Lista todos os livros
     */
    public List<Livro> listarTodos() {
        return livroRepository.findAll();
    }

    /**
     * Exclui um livro pelo ID
     */
    public void excluir(Long id) {
        try {
            livroRepository.delete(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir livro com ID: " + id, e);
            throw new RuntimeException("Erro ao excluir livro: " + e.getMessage(), e);
        }
    }

    /**
     * Pesquisa livros com base em critérios
     */
    public List<Livro> pesquisar(Livro filtro) {
        return livroRepository.search(filtro);
    }

    /**
     * Busca informações do livro pelo ISBN na API do OpenLibrary
     */
    public Optional<Livro> buscarLivroPorIsbnApi(String isbn) {
        // Primeiro verifica se o livro já existe no banco de dados
        Optional<Livro> livroExistente = livroRepository.findByIsbn(isbn);
        if (livroExistente.isPresent()) {
            return livroExistente;
        }

        // Se não encontrado, busca na API
        return openLibraryService.buscarLivroPorIsbn(isbn);
    }

    /**
     * Atualiza livros similares
     */
    public Livro atualizarLivrosSimilares(Livro livro, List<String> isbns) {
        if (livro == null || livro.getId() == null) {
            throw new IllegalArgumentException("Livro precisa estar salvo para atualizar livros similares");
        }

        try {
            // Limpa os livros similares existentes
            livro.getLivrosSimilares().clear();

            // Adiciona os novos livros similares
            for (String isbn : isbns) {
                if (isbn == null || isbn.trim().isEmpty()) {
                    continue;
                }

                // Não adiciona o próprio livro como similar
                if (isbn.equals(livro.getIsbn())) {
                    continue;
                }

                livroRepository.findByIsbn(isbn.trim()).ifPresent(livro::adicionarLivroSimilar);
            }

            // Salva o livro atualizado
            return livroRepository.save(livro);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar livros similares", e);
            throw new RuntimeException("Erro ao atualizar livros similares: " + e.getMessage(), e);
        }
    }

    /**
     * Sugere livros similares com base em autores comuns
     */
    public List<Livro> sugerirLivrosSimilares(Livro livro, int maxResultados) {
        if (livro == null || livro.getId() == null) {
            return new ArrayList<>();
        }

        List<Livro> sugestoes = new ArrayList<>();

        // Se não houver autores, não há como sugerir similares
        if (livro.getAutores() == null || livro.getAutores().isEmpty()) {
            return sugestoes;
        }

        // Busca livros com autores em comum
        List<Livro> todosLivros = livroRepository.findAll();

        for (Livro candidato : todosLivros) {
            // Pula o próprio livro
            if (candidato.getId().equals(livro.getId())) {
                continue;
            }

            // Verifica se há autores em comum
            boolean temAutorEmComum = livro.getAutores().stream()
                    .anyMatch(autor -> candidato.getAutores().contains(autor));

            if (temAutorEmComum) {
                sugestoes.add(candidato);

                // Limita o número de resultados
                if (sugestoes.size() >= maxResultados) {
                    break;
                }
            }
        }

        return sugestoes;
    }
}
