package br.com.hadryan.app.service;

import br.com.hadryan.app.model.repository.RepositoryFactory;
import br.com.hadryan.app.service.factory.ImportService;
import br.com.hadryan.app.service.factory.SimilarBookService;
import br.com.hadryan.app.service.impl.BookServiceImpl;

/**
 * Factory para criação de serviços.
 * Implementa o padrão Factory para centralizar a criação de instâncias de serviços.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class ServiceFactory {

    private static ServiceFactory instance;

    private BookService bookService;
    private ImportService importService;
    private SimilarBookService similarBooksService;
    private OpenLibraryService openLibraryService;

    private ServiceFactory() {
        // Construtor privado para padrão Singleton
    }

    /**
     * Retorna a instância única da factory (Singleton)
     *
     * @return Instância da factory
     */
    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    /**
     * Retorna um serviço de livros
     *
     * @return Instância de BookService
     */
    public BookService getBookService() {
        if (bookService == null) {
            bookService = new BookServiceImpl();
        }
        return bookService;
    }

    /**
     * Retorna um serviço de importação
     *
     * @return Instância de ImportService
     */
    public ImportService getImportService() {
        if (importService == null) {
            importService = new ImportService();
        }
        return importService;
    }

    /**
     * Retorna um serviço de livros similares
     *
     * @return Instância de SimilarBooksService
     */
    public SimilarBookService getSimilarBooksService() {
        if (similarBooksService == null) {
            similarBooksService = new SimilarBookService();
        }
        return similarBooksService;
    }

    /**
     * Retorna um serviço da API OpenLibrary
     *
     * @return Instância de OpenLibraryService
     */
    public OpenLibraryService getOpenLibraryService() {
        if (openLibraryService == null) {
            openLibraryService = new OpenLibraryService();
        }
        return openLibraryService;
    }

    /**
     * Fecha todos os serviços e libera recursos
     */
    public void closeAll() {
        if (bookService != null) {
            bookService.close();
            bookService = null;
        }

        if (importService != null) {
            importService.close();
            importService = null;
        }

        RepositoryFactory.getInstance().closeAll();
    }
}
