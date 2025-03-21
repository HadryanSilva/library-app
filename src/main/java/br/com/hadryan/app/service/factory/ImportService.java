package br.com.hadryan.app.service.factory;

import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.model.repository.BookRepository;
import br.com.hadryan.app.model.repository.RepositoryFactory;
import br.com.hadryan.app.service.strategy.CSVImportStrategy;
import br.com.hadryan.app.service.strategy.ImportStrategy;
import br.com.hadryan.app.service.strategy.XMLImportStrategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço para importação de livros a partir de arquivos.
 * Implementa o padrão Strategy para suportar diferentes formatos de arquivo.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class ImportService {

    private static final Logger LOGGER = Logger.getLogger(ImportService.class.getName());

    private final BookRepository bookRepository;
    private final List<ImportStrategy> strategies;

    /**
     * Constrói o serviço de importação com as estratégias suportadas
     */
    public ImportService() {
        this.bookRepository = RepositoryFactory.getInstance().getBookRepository();

        // Inicializa as estratégias disponíveis
        this.strategies = new ArrayList<>();
        this.strategies.add(new CSVImportStrategy());
        this.strategies.add(new XMLImportStrategy());
    }

    /**
     * Importa livros a partir de um arquivo
     *
     * @param file Arquivo a ser importado
     * @return Número de livros importados
     * @throws IOException se ocorrer um erro de I/O
     * @throws IllegalArgumentException se o formato do arquivo não for suportado
     */
    public int importBooks(File file) throws IOException {
        // Encontra a estratégia adequada para o arquivo
        ImportStrategy strategy = findStrategy(file);
        if (strategy == null) {
            throw new IllegalArgumentException("Formato de arquivo não suportado: " + file.getName());
        }

        // Importa os livros usando a estratégia selecionada
        List<Book> importedBooks = strategy.importBooks(file);

        // Salva os livros no banco de dados
        return saveImportedBooks(importedBooks);
    }

    /**
     * Encontra a estratégia apropriada para o arquivo
     *
     * @param file Arquivo a ser importado
     * @return Estratégia que suporta o formato do arquivo, ou null se nenhuma for encontrada
     */
    private ImportStrategy findStrategy(File file) {
        for (ImportStrategy strategy : strategies) {
            if (strategy.supports(file)) {
                return strategy;
            }
        }
        return null;
    }

    /**
     * Salva os livros importados no banco de dados
     * Atualiza livros existentes se o ISBN já existir
     *
     * @param books Lista de livros a salvar
     * @return Número de livros salvos
     */
    private int saveImportedBooks(List<Book> books) {
        int count = 0;

        for (Book book : books) {
            try {
                // Ignora livros sem ISBN
                if (book.getIsbn() == null || book.getIsbn().isEmpty()) {
                    continue;
                }

                // Verifica se o livro já existe
                Optional<Book> existingBook = bookRepository.findByIsbn(book.getIsbn());

                if (existingBook.isPresent()) {
                    // Atualiza o livro existente
                    Book existing = existingBook.get();

                    // Atualiza os campos se fornecidos na importação
                    if (book.getTitle() != null && !book.getTitle().isEmpty()) {
                        existing.setTitle(book.getTitle());
                    }

                    if (book.getPublicationDate() != null) {
                        existing.setPublicationDate(book.getPublicationDate());
                    }

                    if (book.getPublisher() != null) {
                        existing.setPublisher(book.getPublisher());
                    }

                    // Adiciona novos autores
                    if (book.getAuthors() != null) {
                        for (Author author : book.getAuthors()) {
                            existing.addAuthor(author);
                        }
                    }

                    bookRepository.save(existing);
                } else {
                    // Salva um novo livro
                    bookRepository.save(book);
                }

                count++;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erro ao salvar livro importado: " + book.getIsbn(), e);
            }
        }

        return count;
    }

    /**
     * Fecha os recursos utilizados pelo serviço
     */
    public void close() {
        bookRepository.close();
    }
}
