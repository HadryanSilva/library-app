package br.com.hadryan.app.service;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.service.http.HttpClientWrapper;
import br.com.hadryan.app.service.json.OpenLibraryJsonParser;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serviço para acessar a API do OpenLibrary.
 * Implementa o padrão Adapter para comunicação com serviço externo.
 *
 * @author Hadryan Silva
 * @since 22-03-2025
 */
public class OpenLibraryService {

    private static final Logger LOGGER = Logger.getLogger(OpenLibraryService.class.getName());
    private static final String API_ISBN_URL = "https://openlibrary.org/isbn/";
    private static final String API_BASE_URL = "https://openlibrary.org";
    private static final String API_FORMAT = ".json";

    private final HttpClientWrapper httpClient;
    private final OpenLibraryJsonParser jsonParser;

    /**
     * Construtor padrão que inicializa as dependências
     */
    public OpenLibraryService() {
        this.httpClient = new HttpClientWrapper();
        this.httpClient.setUseCache(true);
        this.jsonParser = new OpenLibraryJsonParser();
    }

    /**
     * Busca dados de um livro pelo ISBN na API do OpenLibrary
     */
    public Optional<Livro> buscarLivroPorIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "ISBN inválido: nulo ou vazio");
            return Optional.empty();
        }

        try {
            isbn = isbn.replaceAll("[^0-9X]", "");

            String jsonResponse = httpClient.fazerRequisicaoGet(API_ISBN_URL + isbn + API_FORMAT);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return Optional.empty();
            }

            Optional<JsonNode> optRootNode = jsonParser.parseJsonToNode(jsonResponse);

            if (!optRootNode.isPresent()) {
                return Optional.empty();
            }

            JsonNode rootNode = optRootNode.get();

            LivroBuilder builder = new LivroBuilder()
                    .comIsbn(isbn)
                    .comTitulo(jsonParser.getTextFieldOrEmpty(rootNode, OpenLibraryJsonParser.JSON_FIELD_TITLE))
                    .comDataPublicacao(jsonParser.getTextFieldOrEmpty(rootNode, OpenLibraryJsonParser.JSON_FIELD_PUBLISH_DATE));

            JsonNode publishers = jsonParser.getArrayNodeOrEmpty(rootNode, OpenLibraryJsonParser.JSON_FIELD_PUBLISHERS);
            if (!publishers.isEmpty()) {
                builder.comEditora(publishers.get(0).asText());
            }

            processarAutores(rootNode, builder);

            return Optional.of(builder.build());

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro de IO ao buscar livro por ISBN: " + isbn, e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro inesperado ao buscar livro por ISBN: " + isbn, e);
            return Optional.empty();
        }
    }

    /**
     * Processa os autores de um livro a partir do JsonNode
     */
    private void processarAutores(JsonNode rootNode, LivroBuilder builder) {
        JsonNode authors = jsonParser.getArrayNodeOrEmpty(rootNode, OpenLibraryJsonParser.JSON_FIELD_AUTHORS);

        if (!authors.isEmpty()) {
            for (JsonNode authorNode : authors) {
                if (authorNode.has(OpenLibraryJsonParser.JSON_FIELD_KEY)) {
                    String authorKey = authorNode.get(OpenLibraryJsonParser.JSON_FIELD_KEY).asText();
                    buscarDetalheAutor(authorKey).ifPresent(builder::comAutor);
                }
            }
        } else {
            JsonNode works = jsonParser.getArrayNodeOrEmpty(rootNode, OpenLibraryJsonParser.JSON_FIELD_WORKS);

            for (JsonNode workNode : works) {
                if (workNode.has(OpenLibraryJsonParser.JSON_FIELD_KEY)) {
                    String workKey = workNode.get(OpenLibraryJsonParser.JSON_FIELD_KEY).asText();
                    buscarAutoresPorWork(workKey).forEach(builder::comAutor);
                }
            }
        }
    }

    /**
     * Busca informações detalhadas de um autor pelo ID/key
     */
    public Optional<Autor> buscarDetalheAutor(String authorKey) {
        try {
            String jsonResponse = httpClient.fazerRequisicaoGet(API_BASE_URL + authorKey + API_FORMAT);

            return jsonParser.parseJsonToNode(jsonResponse)
                    .flatMap(jsonParser::extrairAutorDeNode);

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erro de IO ao buscar detalhes do autor: " + authorKey, e);
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro inesperado ao buscar detalhes do autor: " + authorKey, e);
            return Optional.empty();
        }
    }

    /**
     * Busca informações detalhadas de autores pelo Work ID/key
     */
    public List<Autor> buscarAutoresPorWork(String workKey) {
        List<Autor> autores = new ArrayList<>();

        try {
            String jsonResponse = httpClient.fazerRequisicaoGet(API_BASE_URL + workKey + API_FORMAT);

            Optional<JsonNode> optWorkNode = jsonParser.parseJsonToNode(jsonResponse);
            if (optWorkNode.isPresent()) {
                JsonNode workNode = optWorkNode.get();

                JsonNode authors = jsonParser.getArrayNodeOrEmpty(workNode, OpenLibraryJsonParser.JSON_FIELD_AUTHORS);
                for (JsonNode authorNode : authors) {
                    if (authorNode.has(OpenLibraryJsonParser.JSON_FIELD_AUTHOR) &&
                            authorNode.get(OpenLibraryJsonParser.JSON_FIELD_AUTHOR).has(OpenLibraryJsonParser.JSON_FIELD_KEY)) {

                        String authorKey = authorNode.get(OpenLibraryJsonParser.JSON_FIELD_AUTHOR)
                                .get(OpenLibraryJsonParser.JSON_FIELD_KEY).asText();

                        buscarDetalheAutor(authorKey).ifPresent(autores::add);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar autores do work: " + workKey, e);
        }

        return autores;
    }
}
