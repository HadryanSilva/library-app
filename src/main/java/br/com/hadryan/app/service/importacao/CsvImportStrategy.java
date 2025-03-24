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
import java.util.logging.Level;
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
        int totalLinhas = 0;
        int linhasProcessadas = 0;
        int linhasIgnoradas = 0;

        try (BufferedReader reader = new BufferedReader(
                new FileReader(arquivo))) {

            String linha = reader.readLine();
            if (linha == null) {
                LOGGER.warning("Arquivo CSV vazio ou inválido");
                return livrosImportados; // Arquivo vazio
            }

            String[] cabecalhos = splitCsvLine(linha);

            int idxTitulo = encontrarIndiceColuna(cabecalhos, "titulo");
            int idxIsbn = encontrarIndiceColuna(cabecalhos, "isbn");
            int idxAutor = encontrarIndiceColuna(cabecalhos, "autor", "autores");
            int idxEditora = encontrarIndiceColuna(cabecalhos, "editora");
            int idxDataPub = encontrarIndiceColuna(cabecalhos, "data_publicacao", "data", "publicacao", "datapublicacao");

            if (idxIsbn == -1) {
                LOGGER.warning("Arquivo CSV não contém coluna ISBN, que é obrigatória.");
                return livrosImportados;
            }
            while ((linha = reader.readLine()) != null) {
                totalLinhas++;
                try {
                    String[] valores = splitCsvLine(linha);
                    if (valores.length <= idxIsbn) {
                        linhasIgnoradas++;
                        LOGGER.fine("Linha " + totalLinhas + " tem menos colunas que o necessário. Ignorando.");
                        continue;
                    }

                    String isbn = valores[idxIsbn].trim();
                    if (isbn.isEmpty()) {
                        linhasIgnoradas++;
                        LOGGER.fine("Linha " + totalLinhas + " sem ISBN. Ignorando.");
                        continue;
                    }

                    Livro livro = new Livro();
                    livro.setIsbn(isbn);

                    if (idxTitulo >= 0 && idxTitulo < valores.length) {
                        String titulo = valores[idxTitulo].trim();
                        if (!titulo.isEmpty()) {
                            livro.setTitulo(titulo);
                        } else {
                            livro.setTitulo("Livro sem título (ISBN: " + isbn.substring(Math.max(0, isbn.length() - 6)) + ")");
                        }
                    } else {
                        livro.setTitulo("Livro sem título (ISBN: " + isbn.substring(Math.max(0, isbn.length() - 6)) + ")");
                    }

                    if (idxDataPub >= 0 && idxDataPub < valores.length) {
                        String dataStr = valores[idxDataPub].trim();
                        if (!dataStr.isEmpty()) {
                            livro.setDataPublicacao(dataStr);
                        }
                    }

                    if (idxEditora >= 0 && idxEditora < valores.length) {
                        String nomeEditora = valores[idxEditora].trim();
                        if (!nomeEditora.isEmpty()) {
                            Editora editora = new Editora(nomeEditora);
                            livro.setEditora(editora);
                        }
                    }

                    if (idxAutor >= 0 && idxAutor < valores.length) {
                        String autoresStr = valores[idxAutor].trim();
                        if (!autoresStr.isEmpty()) {
                            String[] nomesAutores = autoresStr.split("[,;]");
                            for (String nomeAutor : nomesAutores) {
                                String nome = nomeAutor.trim();
                                if (!nome.isEmpty()) {
                                    Autor autor = new Autor(nome);
                                    livro.adicionarAutor(autor);
                                }
                            }
                        }
                    }

                    livrosImportados.add(livro);
                    linhasProcessadas++;
                } catch (Exception e) {
                    linhasIgnoradas++;
                    LOGGER.log(Level.WARNING, "Erro ao processar linha CSV " + totalLinhas + ": " + linha, e);
                }
            }
        }

        LOGGER.info("Importação CSV: " + linhasProcessadas + " linhas processadas com sucesso, "
                + linhasIgnoradas + " linhas ignoradas de um total de " + totalLinhas + " linhas no arquivo.");

        return livrosImportados;
    }

    /**
     * Divide uma linha CSV respeitando aspas e escape characters
     */
    private String[] splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (i < line.length() - 1 && line.charAt(i + 1) == '"') {
                    currentValue.append('"');
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        result.add(currentValue.toString());

        return result.toArray(new String[0]);
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