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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementação refinada da estratégia de importação para arquivos de texto com tamanho fixo.
 * Ajuste fino nos limites dos campos para garantir extração precisa.
 */
public class FixedWidthImportStrategy implements ImportStrategy {

    private static final Logger LOGGER = Logger.getLogger(FixedWidthImportStrategy.class.getName());

    @Override
    public boolean suporta(File arquivo) {
        String nome = arquivo.getName().toLowerCase();
        return nome.endsWith(".txt") || nome.endsWith(".dat") || nome.endsWith(".fix");
    }

    @Override
    public List<Livro> importar(File arquivo) throws IOException {
        List<Livro> livrosImportados = new ArrayList<>();
        int totalLinhas = 0;
        int livrosImportadosCount = 0;
        int livrosIgnorados = 0;

        try (BufferedReader reader = new BufferedReader(
                new FileReader(arquivo))) {
            String linha;
            int numeroLinha = 0;
            while ((linha = reader.readLine()) != null) {
                numeroLinha++;
                totalLinhas++;

                LOGGER.fine("Processando linha " + numeroLinha + ": " + linha);
                if (numeroLinha == 1 && podeSerCabecalho(linha)) {
                    LOGGER.fine("Linha 1 identificada como cabeçalho. Ignorando.");
                    continue;
                }
                if (linha.length() < 80) {
                    livrosIgnorados++;
                    LOGGER.warning("Linha " + numeroLinha + " muito curta. Ignorando.");
                    continue;
                }

                try {
                    Livro livro = processarLinhaComRegex(linha, numeroLinha);

                    if (livro != null) {
                        livrosImportados.add(livro);
                        livrosImportadosCount++;
                    } else {
                        livrosIgnorados++;
                    }

                } catch (Exception e) {
                    livrosIgnorados++;
                    LOGGER.log(Level.WARNING, "Erro ao processar linha " + numeroLinha + ": " + e.getMessage(), e);
                }
            }
        }

        LOGGER.info("Importação de arquivo de largura fixa: " + livrosImportadosCount +
                " livros importados, " + livrosIgnorados + " livros ignorados de um total de " +
                totalLinhas + " linhas.");

        return livrosImportados;
    }

    /**
     * Processa uma linha usando expressões regulares para identificar campos com mais precisão.
     * Este método tenta identificar padrões específicos dentro da linha, como o ISBN.
     */
    private Livro processarLinhaComRegex(String linha, int numeroLinha) {
        Pattern isbnPattern = Pattern.compile("\\s+(97[89][\\d-]{10,14}|[\\dX-]{10,13})\\s+");
        Matcher isbnMatcher = isbnPattern.matcher(linha);

        if (!isbnMatcher.find()) {
            LOGGER.warning("Linha " + numeroLinha + ": Não foi possível encontrar um ISBN válido.");
            return null;
        }

        int isbnStartPos = isbnMatcher.start();
        int isbnEndPos = isbnMatcher.end();
        String isbn = isbnMatcher.group().trim();

        String titulo = "";
        String livrosSimilares = "";
        String dataPublicacao = "";
        String autores;
        String editora;

        try {
            Pattern tituloEndPattern = Pattern.compile("\\S+(?=\\s{2,})");
            Matcher tituloMatcher = tituloEndPattern.matcher(linha.substring(0, isbnStartPos));
            int tituloEndPos = 0;
            if (tituloMatcher.find()) {
                tituloEndPos = tituloMatcher.end();
                titulo = linha.substring(0, tituloEndPos).trim();
            }

            String middlePart = linha.substring(tituloEndPos, isbnStartPos).trim();
            Pattern dataPattern = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");
            Matcher dataMatcher = dataPattern.matcher(middlePart);

            if (dataMatcher.find()) {
                int dataStartPos = dataMatcher.start();
                dataPublicacao = dataMatcher.group().trim();
                autores = middlePart.substring(0, dataStartPos).trim();
            } else {
                autores = middlePart;
            }

            String afterISBN = linha.substring(isbnEndPos).trim();
            int editoraEndPos = afterISBN.length();

            Pattern similaresStartPattern = Pattern.compile("\\s{2,}");
            Matcher similaresStartMatcher = similaresStartPattern.matcher(afterISBN);

            if (similaresStartMatcher.find()) {
                editoraEndPos = similaresStartMatcher.start();
                livrosSimilares = afterISBN.substring(similaresStartMatcher.end()).trim();
            }

            editora = afterISBN.substring(0, editoraEndPos).trim();

            LOGGER.fine("Linha " + numeroLinha + " - Extraído por regex: Título: [" + titulo +
                    "], Autores: [" + autores + "], Data: [" + dataPublicacao +
                    "], ISBN: [" + isbn + "], Editora: [" + editora +
                    "], Similares: [" + livrosSimilares + "]");

            Livro livro = new Livro();
            livro.setIsbn(isbn);

            if (!titulo.isEmpty()) {
                livro.setTitulo(titulo);
            } else {
                livro.setTitulo("Livro sem título (ISBN: " + isbn.substring(Math.max(0, isbn.length() - 6)) + ")");
            }
            if (!editora.isEmpty()) {
                livro.setEditora(new Editora(editora));
            }
            if (!dataPublicacao.isEmpty()) {
                livro.setDataPublicacao(dataPublicacao);
            }
            processarAutores(livro, autores);

            return livro;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao processar campos da linha " + numeroLinha + ": " + e.getMessage(), e);
            Livro livroMinimo = new Livro();
            livroMinimo.setIsbn(isbn);
            livroMinimo.setTitulo("Livro importado com erros (ISBN: " + isbn + ")");
            return livroMinimo;
        }
    }

    /**
     * Processa a string de autores e adiciona ao livro
     */
    private void processarAutores(Livro livro, String autoresStr) {
        if (autoresStr.isEmpty()) {
            return;
        }

        String[] autoresArray = autoresStr.split("[,;]");
        for (String nomeAutor : autoresArray) {
            String nome = nomeAutor.trim();
            if (!nome.isEmpty()) {
                livro.adicionarAutor(new Autor(nome));
            }
        }
    }

    /**
     * Tenta identificar se a linha é um cabeçalho baseado em heurísticas simples
     */
    private boolean podeSerCabecalho(String linha) {
        String linhaBaixa = linha.toLowerCase();
        return linhaBaixa.contains("isbn") ||
                linhaBaixa.contains("titulo") ||
                linhaBaixa.contains("autor");
    }
}
