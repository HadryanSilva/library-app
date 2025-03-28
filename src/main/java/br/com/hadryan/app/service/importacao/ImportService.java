package br.com.hadryan.app.service.importacao;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.model.repository.LivroRepository;

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
 * @since 22-03-2025
 */
public class ImportService {

    private static final Logger LOGGER = Logger.getLogger(ImportService.class.getName());

    private final List<ImportStrategy> estrategias;
    private final LivroRepository livroRepository;

    /**
     * Construtor que inicializa estratégias padrão
     */
    public ImportService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;

        this.estrategias = new ArrayList<>();
        this.estrategias.add(new CsvImportStrategy());
        this.estrategias.add(new XmlImportStrategy());
        this.estrategias.add(new FixedWidthImportStrategy());
    }

    /**
     * Importa livros a partir de um arquivo
     *
     * @param arquivo Arquivo a ser importado
     * @return Número de livros importados
     * @throws IOException se ocorrer um erro de I/O
     * @throws IllegalArgumentException se o formato do arquivo não for suportado
     */
    public int importarLivros(File arquivo) throws IOException {
        ImportStrategy estrategia = encontrarEstrategia(arquivo);
        if (estrategia == null) {
            throw new IllegalArgumentException("Formato de arquivo não suportado: " + arquivo.getName());
        }
        List<Livro> livrosImportados = estrategia.importar(arquivo);

        return salvarLivrosImportados(livrosImportados);
    }

    /**
     * Encontra a estratégia apropriada para o arquivo
     */
    private ImportStrategy encontrarEstrategia(File arquivo) {
        for (ImportStrategy estrategia : estrategias) {
            if (estrategia.suporta(arquivo)) {
                return estrategia;
            }
        }
        return null;
    }

    /**
     * Salva os livros importados no banco de dados
     * Atualiza livros existentes se o ISBN já existir
     */
    private int salvarLivrosImportados(List<Livro> livros) {
        int contador = 0;

        for (Livro livro : livros) {
            try {
                if (livro.getIsbn() == null || livro.getIsbn().isEmpty()) {
                    continue;
                }

                Optional<Livro> livroExistente = livroRepository.findByIsbn(livro.getIsbn());

                if (livroExistente.isPresent()) {
                    Livro existente = livroExistente.get();

                    if (livro.getTitulo() != null && !livro.getTitulo().isEmpty()) {
                        existente.setTitulo(livro.getTitulo());
                    }

                    if (livro.getDataPublicacao() != null) {
                        existente.setDataPublicacao(livro.getDataPublicacao());
                    }

                    if (livro.getEditora() != null) {
                        existente.setEditora(livro.getEditora());
                    }

                    if (livro.getAutores() != null) {
                        for (Autor autor : livro.getAutores()) {
                            existente.adicionarAutor(autor);
                        }
                    }

                    livroRepository.save(existente);
                } else {
                    livroRepository.save(livro);
                }

                contador++;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erro ao salvar livro importado: " + livro.getIsbn(), e);
            }
        }

        return contador;
    }
}