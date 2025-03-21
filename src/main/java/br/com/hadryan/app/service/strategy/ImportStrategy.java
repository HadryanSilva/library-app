package br.com.hadryan.app.service.strategy;

import br.com.hadryan.app.model.entity.Book;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface para o padrão Strategy de importação de livros.
 * Define a estratégia para importação de livros de diferentes formatos de arquivo.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public interface ImportStrategy {

    /**
     * Importa livros de um arquivo
     *
     * @param file Arquivo a ser importado
     * @return Lista de livros importados
     * @throws IOException se ocorrer um erro de I/O
     */
    List<Book> importBooks(File file) throws IOException;

    /**
     * Verifica se a estratégia suporta o formato do arquivo
     *
     * @param file Arquivo a ser verificado
     * @return true se a estratégia suporta o formato, false caso contrário
     */
    boolean supports(File file);
}