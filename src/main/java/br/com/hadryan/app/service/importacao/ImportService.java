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
 */
public class ImportService {

    private static final Logger LOGGER = Logger.getLogger(ImportService.class.getName());

    private final List<ImportStrategy> estrategias;
    private final LivroRepository livroRepository;

    /**
     * Construtor que recebe o repositório e as estratégias disponíveis
     */
    public ImportService(LivroRepository livroRepository, List<ImportStrategy> estrategias) {
        this.livroRepository = livroRepository;
        this.estrategias = estrategias;
    }

    /**
     * Construtor que inicializa estratégias padrão
     */
    public ImportService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;

        // Inicializa as estratégias disponíveis
        this.estrategias = new ArrayList<>();
        this.estrategias.add(new CsvImportStrategy());
        this.estrategias.add(new XmlImportStrategy());
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
        // Encontra a estratégia adequada para o arquivo
        ImportStrategy estrategia = encontrarEstrategia(arquivo);
        if (estrategia == null) {
            throw new IllegalArgumentException("Formato de arquivo não suportado: " + arquivo.getName());
        }

        // Importa os livros usando a estratégia selecionada
        List<Livro> livrosImportados = estrategia.importar(arquivo);

        // Salva os livros no banco de dados
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
                // Ignora livros sem ISBN
                if (livro.getIsbn() == null || livro.getIsbn().isEmpty()) {
                    continue;
                }

                // Verifica se o livro já existe
                Optional<Livro> livroExistente = livroRepository.findByIsbn(livro.getIsbn());

                if (livroExistente.isPresent()) {
                    // Atualiza o livro existente
                    Livro existente = livroExistente.get();

                    // Atualiza os campos se fornecidos na importação
                    if (livro.getTitulo() != null && !livro.getTitulo().isEmpty()) {
                        existente.setTitulo(livro.getTitulo());
                    }

                    if (livro.getDataPublicacao() != null) {
                        existente.setDataPublicacao(livro.getDataPublicacao());
                    }

                    if (livro.getEditora() != null) {
                        existente.setEditora(livro.getEditora());
                    }

                    // Adiciona novos autores
                    if (livro.getAutores() != null) {
                        for (Autor autor : livro.getAutores()) {
                            existente.adicionarAutor(autor);
                        }
                    }

                    livroRepository.save(existente);
                } else {
                    // Salva um novo livro
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