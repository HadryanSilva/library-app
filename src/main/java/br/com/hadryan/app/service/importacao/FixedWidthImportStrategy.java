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
 * Implementação concreta da estratégia de importação para arquivos de texto com tamanho fixo.
 * Processa arquivos onde cada campo tem uma posição e comprimento fixos.
 */
public class FixedWidthImportStrategy implements ImportStrategy {

    private static final Logger LOGGER = Logger.getLogger(FixedWidthImportStrategy.class.getName());

    // Definição da estrutura do arquivo (posições iniciais e comprimentos)
    private static final int ISBN_START = 0;
    private static final int ISBN_LENGTH = 20;

    private static final int TITULO_START = 20;
    private static final int TITULO_LENGTH = 100;

    private static final int AUTORES_START = 120;
    private static final int AUTORES_LENGTH = 100;

    private static final int EDITORA_START = 220;
    private static final int EDITORA_LENGTH = 50;

    private static final int DATA_PUB_START = 270;
    private static final int DATA_PUB_LENGTH = 30;

    @Override
    public boolean suporta(File arquivo) {
        // Verifica se o arquivo tem extensão .txt ou .dat (comuns para arquivos de tamanho fixo)
        String nome = arquivo.getName().toLowerCase();
        return nome.endsWith(".txt") || nome.endsWith(".dat") || nome.endsWith(".fix");
    }

    @Override
    public List<Livro> importar(File arquivo) throws IOException {
        List<Livro> livrosImportados = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int numeroLinha = 0;

            // Lê cada linha do arquivo
            while ((linha = reader.readLine()) != null) {
                numeroLinha++;

                // Pula a primeira linha se for cabeçalho (opcional)
                if (numeroLinha == 1 && podeSerCabecalho(linha)) {
                    continue;
                }

                // Verifica se a linha tem o tamanho mínimo necessário
                if (linha.length() < DATA_PUB_START) {
                    LOGGER.warning("Linha " + numeroLinha + " muito curta. Ignorando.");
                    continue;
                }

                try {
                    // Extrai os campos com base nas posições definidas
                    String isbn = extrairCampo(linha, ISBN_START, ISBN_LENGTH).trim();

                    // ISBN é um campo obrigatório, pula linhas sem ISBN
                    if (isbn.isEmpty()) {
                        LOGGER.warning("Linha " + numeroLinha + " sem ISBN. Ignorando.");
                        continue;
                    }

                    String titulo = extrairCampo(linha, TITULO_START, TITULO_LENGTH).trim();
                    String autoresStr = extrairCampo(linha, AUTORES_START, AUTORES_LENGTH).trim();
                    String editora = extrairCampo(linha, EDITORA_START, EDITORA_LENGTH).trim();
                    String dataPublicacao = extrairCampo(linha, DATA_PUB_START, DATA_PUB_LENGTH).trim();

                    // Cria o livro com os dados extraídos
                    Livro livro = new Livro();
                    livro.setIsbn(isbn);
                    livro.setTitulo(titulo);

                    // Define a editora se existir
                    if (!editora.isEmpty()) {
                        livro.setEditora(new Editora(editora));
                    }

                    // Define a data de publicação se existir
                    if (!dataPublicacao.isEmpty()) {
                        livro.setDataPublicacao(dataPublicacao);
                    }

                    // Processa e adiciona os autores
                    processarAutores(livro, autoresStr);

                    livrosImportados.add(livro);

                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Erro ao processar linha " + numeroLinha, e);
                }
            }
        }

        return livrosImportados;
    }

    /**
     * Extrai um campo da linha de acordo com a posição e comprimento definidos
     */
    private String extrairCampo(String linha, int posicaoInicial, int comprimento) {
        int fim = Math.min(posicaoInicial + comprimento, linha.length());
        return posicaoInicial < linha.length() ? linha.substring(posicaoInicial, fim) : "";
    }

    /**
     * Processa a string de autores e adiciona ao livro
     */
    private void processarAutores(Livro livro, String autoresStr) {
        if (autoresStr.isEmpty()) {
            return;
        }

        // Assume que os autores estão separados por ponto-e-vírgula
        String[] autoresArray = autoresStr.split(";");
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
        // Considera uma linha como cabeçalho se contém palavras típicas de cabeçalho
        // e não contém ISBN válido
        String linhaBaixa = linha.toLowerCase();
        boolean temPalavrasCabecalho = linhaBaixa.contains("isbn") ||
                linhaBaixa.contains("titulo") ||
                linhaBaixa.contains("autor");

        // Extrai o campo onde estaria o ISBN
        String possiveIsbn = extrairCampo(linha, ISBN_START, ISBN_LENGTH).trim();
        // Verifica se o possível ISBN parece válido (apenas dígitos e X)
        boolean naoTemIsbnValido = !possiveIsbn.matches("[0-9X-]+");

        return temPalavrasCabecalho && naoTemIsbnValido;
    }
}
