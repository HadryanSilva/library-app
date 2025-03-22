package br.com.hadryan.app.service.importacao;

import br.com.hadryan.app.model.entity.Livro;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface para o padrão Strategy de importação de livros.
 * Define a estratégia para importação de livros de diferentes formatos de arquivo.
 */
public interface ImportStrategy {

    /**
     * Verifica se a estratégia suporta o formato do arquivo
     *
     * @param arquivo Arquivo a ser verificado
     * @return true se a estratégia suporta o formato, false caso contrário
     */
    boolean suporta(File arquivo);

    /**
     * Importa livros de um arquivo
     *
     * @param arquivo Arquivo a ser importado
     * @return Lista de livros importados
     * @throws IOException se ocorrer um erro de I/O
     */
    List<Livro> importar(File arquivo) throws IOException;
}
