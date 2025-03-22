package br.com.hadryan.app.service.importacao;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementação concreta da estratégia de importação para arquivos CSV.
 * Adaptada para tratar data de publicação como String.
 */
public class CsvImportStrategy implements ImportStrategy {

    private static final Logger LOGGER = Logger.getLogger(CsvImportStrategy.class.getName());

    @Override
    public boolean suporta(File arquivo) {
        return arquivo.getName().toLowerCase().endsWith(".csv");
    }

    @Override
    public List<Livro> importar(File arquivo) throws IOException {
        List<Livro> livrosImportados = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha = reader.readLine(); // Linha de cabeçalho
            if (linha == null) {
                return livrosImportados; // Arquivo vazio
            }

            String[] cabecalhos = linha.split(",");

            // Mapeia os índices das colunas
            int idxTitulo = encontrarIndiceColuna(cabecalhos, "titulo");
            int idxIsbn = encontrarIndiceColuna(cabecalhos, "isbn");
            int idxAutor = encontrarIndiceColuna(cabecalhos, "autor", "autores");
            int idxEditora = encontrarIndiceColuna(cabecalhos, "editora");
            int idxDataPub = encontrarIndiceColuna(cabecalhos, "data_publicacao", "data", "publicacao");

            if (idxIsbn == -1) {
                LOGGER.warning("Arquivo CSV não contém coluna ISBN, que é obrigatória.");
                return livrosImportados;
            }

            // Lê as linhas de dados
            while ((linha = reader.readLine()) != null) {
                String[] valores = linha.split(",");

                // Pula linhas vazias ou mal formatadas
                if (valores.length <= idxIsbn) {
                    continue;
                }

                // ISBN é obrigatório
                String isbn = valores[idxIsbn].trim();
                if (isbn.isEmpty()) {
                    continue; // Pula linhas sem ISBN
                }

                Livro livro = new Livro();
                livro.setIsbn(isbn);

                // Título
                if (idxTitulo >= 0 && idxTitulo < valores.length) {
                    livro.setTitulo(valores[idxTitulo].trim());
                }

                // Data de publicação - armazena diretamente como String
                if (idxDataPub >= 0 && idxDataPub < valores.length) {
                    String dataStr = valores[idxDataPub].trim();
                    if (!dataStr.isEmpty()) {
                        livro.setDataPublicacao(dataStr);
                    }
                }

                // Editora
                if (idxEditora >= 0 && idxEditora < valores.length) {
                    String nomeEditora = valores[idxEditora].trim();
                    if (!nomeEditora.isEmpty()) {
                        Editora editora = new Editora(nomeEditora);
                        livro.setEditora(editora);
                    }
                }

                // Autores
                if (idxAutor >= 0 && idxAutor < valores.length) {
                    String[] nomesAutores = valores[idxAutor].split(";");
                    for (String nomeAutor : nomesAutores) {
                        String nome = nomeAutor.trim();
                        if (!nome.isEmpty()) {
                            Autor autor = new Autor(nome);
                            livro.adicionarAutor(autor);
                        }
                    }
                }

                livrosImportados.add(livro);
            }
        }

        return livrosImportados;
    }

    /**
     * Encontra o índice de uma coluna pelos possíveis nomes
     */
    private int encontrarIndiceColuna(String[] cabecalhos, String... possiveisNomes) {
        for (int i = 0; i < cabecalhos.length; i++) {
            String cabecalhoNormalizado = cabecalhos[i].trim().toLowerCase();
            for (String nome : possiveisNomes) {
                if (cabecalhoNormalizado.equals(nome.toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }
}