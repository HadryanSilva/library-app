package br.com.hadryan.app.service.factory;

import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.model.repository.BookRepository;
import br.com.hadryan.app.model.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço para gerenciar livros similares.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class SimilarBookService {

    private static final Logger LOGGER = Logger.getLogger(SimilarBookService.class.getName());

    private final BookRepository bookRepository;

    public SimilarBookService() {
        this.bookRepository = RepositoryFactory.getInstance().getBookRepository();
    }

    /**
     * Busca livros similares por ISBN
     *
     * @param isbnList Lista de ISBNs dos livros similares
     * @return Lista de livros encontrados
     */
    public List<Book> findSimilarBooksByIsbn(List<String> isbnList) {
        List<Book> similarBooks = new ArrayList<>();

        for (String isbn : isbnList) {
            if (isbn == null || isbn.trim().isEmpty()) {
                continue;
            }

            bookRepository.findByIsbn(isbn.trim()).ifPresent(similarBooks::add);
        }

        return similarBooks;
    }

    /**
     * Atualiza os livros similares de um livro
     *
     * @param book Livro a ser atualizado
     * @param similarIsbnList Lista de ISBNs dos livros similares
     * @return Livro atualizado
     */
    public Book updateSimilarBooks(Book book, List<String> similarIsbnList) {
        try {
            // Limpa a lista atual de livros similares
            Set<Book> currentSimilarBooks = new HashSet<>(book.getSimilarBooks());
            for (Book similar : currentSimilarBooks) {
                book.removeSimilarBook(similar);
            }

            // Adiciona os novos livros similares
            List<Book> similarBooks = findSimilarBooksByIsbn(similarIsbnList);
            for (Book similar : similarBooks) {
                if (!book.equals(similar)) { // Evita auto-referência
                    book.addSimilarBook(similar);
                }
            }

            // Salva o livro atualizado
            return bookRepository.save(book);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar livros similares: " + e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar livros similares: " + e.getMessage(), e);
        }
    }

    /**
     * Sugere livros similares com base nos livros existentes
     * baseado em autores comuns
     *
     * @param book Livro para sugerir similares
     * @param maxResults Número máximo de resultados
     * @return Lista de livros similares sugeridos
     */
    public List<Book> suggestSimilarBooks(Book book, int maxResults) {
        List<Book> suggestions = new ArrayList<>();

        // Implementação simples: livros do mesmo autor
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            for (Book existingBook : bookRepository.findAll()) {
                // Pula o próprio livro
                if (existingBook.getId().equals(book.getId())) {
                    continue;
                }

                // Verifica se há autores em comum
                for (Author author : book.getAuthors()) {
                    if (existingBook.getAuthors().contains(author)) {
                        suggestions.add(existingBook);
                        break;
                    }
                }

                // Limita o número de resultados
                if (suggestions.size() >= maxResults) {
                    break;
                }
            }
        }

        return suggestions;
    }
}
