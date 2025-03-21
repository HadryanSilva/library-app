package br.com.hadryan.app.model.repository;

/**
 * Factory para criação de repositórios.
 * Implementa o padrão Factory para centralizar a criação de instâncias de repositórios.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class RepositoryFactory {
    private static RepositoryFactory instance;

    private BookRepository bookRepository;
    private AuthorRepository authorRepository;
    private PublisherRepository publisherRepository;

    private RepositoryFactory() {
        // Construtor privado para padrão Singleton
    }

    /**
     * Retorna a instância única da factory (Singleton)
     *
     * @return Instância da factory
     */
    public static synchronized RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }

    /**
     * Retorna um repositório de livros
     *
     * @return Instância de BookRepository
     */
    public BookRepository getBookRepository() {
        if (bookRepository == null) {
            bookRepository = new BookRepository();
        }
        return bookRepository;
    }

    /**
     * Retorna um repositório de autores
     *
     * @return Instância de AuthorRepository
     */
    public AuthorRepository getAuthorRepository() {
        if (authorRepository == null) {
            authorRepository = new AuthorRepository();
        }
        return authorRepository;
    }

    /**
     * Retorna um repositório de editoras
     *
     * @return Instância de PublisherRepository
     */
    public PublisherRepository getPublisherRepository() {
        if (publisherRepository == null) {
            publisherRepository = new PublisherRepository();
        }
        return publisherRepository;
    }

    /**
     * Fecha todos os repositórios e libera recursos
     */
    public void closeAll() {
        if (bookRepository != null) {
            bookRepository.close();
            bookRepository = null;
        }

        if (authorRepository != null) {
            authorRepository.close();
            authorRepository = null;
        }

        if (publisherRepository != null) {
            publisherRepository.close();
            publisherRepository = null;
        }
    }
}
