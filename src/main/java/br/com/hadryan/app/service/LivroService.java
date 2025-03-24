package br.com.hadryan.app.service;

import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.model.repository.LivroRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço responsável pela lógica de negócio relacionada a livros.
 *
 * @author Hadryan Silva
 * @since 23-03-2025
 */
public class LivroService {

    private static final Logger LOGGER = Logger.getLogger(LivroService.class.getName());

    private final LivroRepository livroRepository;
    private final OpenLibraryService openLibraryService;
    private final WorkSubjectService workSubjectService;

    /**
     * Construtor com injeção de dependências
     */
    public LivroService(LivroRepository livroRepository, OpenLibraryService openLibraryService) {
        this.livroRepository = livroRepository;
        this.openLibraryService = openLibraryService;
        this.workSubjectService = new WorkSubjectService(openLibraryService);
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
        Optional<Livro> livroExistente = livroRepository.findByIsbn(isbn);
        if (livroExistente.isPresent()) {
            return livroExistente;
        }
        return openLibraryService.buscarLivroPorIsbn(isbn);
    }

    /**
     * Atualiza livros similares
     */
    public void atualizarLivrosSimilares(Livro livro, List<String> isbns) {
        if (livro == null || livro.getId() == null) {
            throw new IllegalArgumentException("Livro precisa estar salvo para atualizar livros similares");
        }
        try {
            livro.getLivrosSimilares().clear();
            for (String isbn : isbns) {
                if (isbn == null || isbn.trim().isEmpty()) {
                    continue;
                }
                if (isbn.equals(livro.getIsbn())) {
                    continue;
                }
                livroRepository.findByIsbn(isbn.trim()).ifPresent(livro::adicionarLivroSimilar);
            }
            livroRepository.save(livro);
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

        if (livro.getAutores() == null || livro.getAutores().isEmpty()) {
            return sugestoes;
        }
        List<Livro> todosLivros = livroRepository.findAll();
        for (Livro candidato : todosLivros) {
            if (candidato.getId().equals(livro.getId())) {
                continue;
            }

            boolean temAutorEmComum = livro.getAutores().stream()
                    .anyMatch(autor -> candidato.getAutores().contains(autor));

            if (temAutorEmComum) {
                sugestoes.add(candidato);
                if (sugestoes.size() >= maxResultados) {
                    break;
                }
            }
        }

        return sugestoes;
    }

    /**
     * Busca livros relacionados por assuntos (subjects) via API
     */
    public List<Livro> buscarLivrosRelacionadosPorSubjects(String isbn, int maxResultados) {
        try {
            return workSubjectService.buscarLivrosRelacionados(isbn, maxResultados);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar livros relacionados por subjects", e);
            return new ArrayList<>();
        }
    }
}
