package br.com.hadryan.app.service.api;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço para acessar a API do OpenLibrary.
 * Implementa o padrão Adapter para comunicação com serviço externo.
 */
public class OpenLibraryService {

    private static final Logger LOGGER = Logger.getLogger(OpenLibraryService.class.getName());
    private static final String API_URL = "https://openlibrary.org/isbn/";
    private static final String API_BASE_URL = "https://openlibrary.org";
    private static final String API_FORMAT = ".json";

    private final ObjectMapper objectMapper;

    /**
     * Construtor padrão
     */
    public OpenLibraryService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Busca dados de um livro pelo ISBN na API do OpenLibrary
     *
     * @param isbn ISBN do livro a buscar
     * @return Optional contendo o livro se encontrado
     */
    public Optional<Livro> buscarLivroPorIsbn(String isbn) {
        try {
            isbn = isbn.replaceAll("[^0-9X]", "");
            String jsonResponse = fazerRequisicaoGet(API_URL + isbn + API_FORMAT);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return Optional.empty();
            }

            // Faz o parse do JSON usando Jackson
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Converte para o modelo de domínio
            Livro livro = converterParaLivro(rootNode, isbn);
            return Optional.of(livro);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar livro por ISBN: " + isbn, e);
            return Optional.empty();
        }
    }

    /**
     * Faz uma requisição GET para a API
     *
     * @param urlStr URL para a requisição
     * @return String contendo a resposta JSON
     * @throws IOException em caso de erro na requisição
     */
    private String fazerRequisicaoGet(String urlStr) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            } else {
                LOGGER.log(Level.WARNING, "Resposta não-OK da API: " + responseCode);
                return null;
            }

        } finally {
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return response.toString();
    }

    /**
     * Converte os dados JSON para um objeto Livro
     *
     * @param rootNode JsonNode contendo os dados do livro
     * @param isbn ISBN do livro
     * @return Objeto Livro preenchido com os dados do JSON
     */
    private Livro converterParaLivro(JsonNode rootNode, String isbn) {
        Livro livro = new Livro();
        livro.setIsbn(isbn);

        // Título
        if (rootNode.has("title")) {
            livro.setTitulo(rootNode.get("title").asText());
        }

        if (rootNode.has("publish_date")) {
            String dataPublicacaoStr = rootNode.get("publish_date").asText();
            livro.setDataPublicacao(dataPublicacaoStr);
        }

        // Editora
        if (rootNode.has("publishers") && rootNode.get("publishers").isArray()) {
            JsonNode publishers = rootNode.get("publishers");
            if (!publishers.isEmpty()) {
                String nomeEditora = publishers.get(0).asText();
                livro.setEditora(new Editora(nomeEditora));
            }
        }

        if (rootNode.has("authors") && rootNode.get("authors").isArray()) {
            JsonNode authors = rootNode.get("authors");
            for (JsonNode authorNode : authors) {
                if (authorNode.has("key")) {
                    String authorKey = authorNode.get("key").asText();
                    buscarDetalheAutor(authorKey).ifPresent(livro::adicionarAutor);
                }
            }
        }

        return livro;
    }

    /**
     * Busca informações detalhadas de um autor pelo ID/key
     *
     * @param authorKey Chave do autor (/authors/OLXXXXA)
     * @return Optional com o autor, se encontrado
     */
    private Optional<Autor> buscarDetalheAutor(String authorKey) {
        try {
            // Faz a requisição para a API de autores
            String jsonResponse = fazerRequisicaoGet(API_BASE_URL + authorKey + ".json");

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return Optional.empty();
            }

            JsonNode authorNode = objectMapper.readTree(jsonResponse);

            if (authorNode.has("name")) {
                String nomeAutor = authorNode.get("name").asText();
                return Optional.of(new Autor(nomeAutor));
            }

            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar detalhes do autor: " + authorKey, e);
            return Optional.empty();
        }
    }
}
