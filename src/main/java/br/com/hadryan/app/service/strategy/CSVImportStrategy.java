package br.com.hadryan.app.service.strategy;

import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.model.entity.Publisher;
import br.com.hadryan.app.model.repository.AuthorRepository;
import br.com.hadryan.app.model.repository.PublisherRepository;
import br.com.hadryan.app.model.repository.RepositoryFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementação concreta da estratégia de importação para arquivos CSV.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class CSVImportStrategy implements ImportStrategy {

    private static final Logger LOGGER = Logger.getLogger(CSVImportStrategy.class.getName());

    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;

    public CSVImportStrategy() {
        RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
        this.authorRepository = repositoryFactory.getAuthorRepository();
        this.publisherRepository = repositoryFactory.getPublisherRepository();
    }

    @Override
    public List<Book> importBooks(File file) throws IOException {
        List<Book> importedBooks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Header line
            if (line == null) {
                return importedBooks; // Arquivo vazio
            }

            String[] headers = line.split(",");

            // Map column indices
            int titleIndex = findColumnIndex(headers, "title");
            int isbnIndex = findColumnIndex(headers, "isbn");
            int authorIndex = findColumnIndex(headers, "author", "authors");
            int publisherIndex = findColumnIndex(headers, "publisher");
            int pubDateIndex = findColumnIndex(headers, "publication_date", "date", "pubdate");

            if (isbnIndex == -1) {
                LOGGER.warning("Arquivo CSV não contém coluna ISBN, que é obrigatória.");
                return importedBooks;
            }

            // Read data lines
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                // Pular linhas vazias ou mal formatadas
                if (values.length < Math.max(1, Math.max(titleIndex, Math.max(isbnIndex, Math.max(authorIndex, Math.max(publisherIndex, pubDateIndex)))))) {
                    continue;
                }

                Book book = new Book();

                // ISBN é obrigatório
                if (isbnIndex >= 0 && isbnIndex < values.length) {
                    String isbn = values[isbnIndex].trim();
                    if (isbn.isEmpty()) {
                        continue; // Pular registros sem ISBN
                    }
                    book.setIsbn(isbn);
                } else {
                    continue; // Pular se não tiver ISBN ou estiver fora dos limites
                }

                // Título
                if (titleIndex >= 0 && titleIndex < values.length) {
                    book.setTitle(values[titleIndex].trim());
                }

                // Data de publicação
                if (pubDateIndex >= 0 && pubDateIndex < values.length && !values[pubDateIndex].trim().isEmpty()) {
                    try {
                        String dateStr = values[pubDateIndex].trim();
                        LocalDate pubDate = LocalDate.parse(dateStr, dateFormatter);
                        book.setPublicationDate(pubDate);
                    } catch (DateTimeParseException e) {
                        LOGGER.log(Level.WARNING, "Erro ao converter data: " + values[pubDateIndex], e);
                    }
                }

                // Editora
                if (publisherIndex >= 0 && publisherIndex < values.length && !values[publisherIndex].trim().isEmpty()) {
                    String publisherName = values[publisherIndex].trim();
                    Publisher publisher = publisherRepository.findByName(publisherName)
                            .orElse(new Publisher(publisherName));
                    book.setPublisher(publisher);
                }

                // Autores
                if (authorIndex >= 0 && authorIndex < values.length && !values[authorIndex].trim().isEmpty()) {
                    String[] authorNames = values[authorIndex].trim().split(";");
                    for (String authorName : authorNames) {
                        String name = authorName.trim();
                        if (!name.isEmpty()) {
                            Author author = authorRepository.findByName(name)
                                    .orElse(new Author(name));
                            book.addAuthor(author);
                        }
                    }
                }

                importedBooks.add(book);
            }
        }

        return importedBooks;
    }

    @Override
    public boolean supports(File file) {
        return file.getName().toLowerCase().endsWith(".csv");
    }

    /**
     * Encontra o índice de uma coluna pelos possíveis nomes
     *
     * @param headers Array com os cabeçalhos
     * @param possibleNames Possíveis nomes da coluna
     * @return O índice da coluna ou -1 se não encontrada
     */
    private int findColumnIndex(String[] headers, String... possibleNames) {
        for (int i = 0; i < headers.length; i++) {
            String normalizedHeader = headers[i].trim().toLowerCase();
            for (String name : possibleNames) {
                if (normalizedHeader.equals(name.toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }
}