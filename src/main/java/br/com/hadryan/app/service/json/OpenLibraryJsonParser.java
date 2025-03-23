package br.com.hadryan.app.service.json;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenLibraryJsonParser {

    private static final Logger LOGGER = Logger.getLogger(OpenLibraryJsonParser.class.getName());

    public static final String JSON_FIELD_TITLE = "title";
    public static final String JSON_FIELD_PUBLISHERS = "publishers";
    public static final String JSON_FIELD_PUBLISH_DATE = "publish_date";
    public static final String JSON_FIELD_AUTHORS = "authors";
    public static final String JSON_FIELD_AUTHOR = "author";
    public static final String JSON_FIELD_WORKS = "works";
    public static final String JSON_FIELD_KEY = "key";
    public static final String JSON_FIELD_NAME = "name";

    private final ObjectMapper objectMapper;

    /**
     * Construtor padrão
     */
    public OpenLibraryJsonParser() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Construtor que permite injeção do ObjectMapper
     */
    public OpenLibraryJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converte uma string JSON para um JsonNode
     */
    public Optional<JsonNode> parseJsonToNode(String json) {
        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readTree(json));
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Erro ao fazer parsing do JSON", e);
            return Optional.empty();
        }
    }

    /**
     * Converte um JsonNode para um objeto Livro
     */
    public Livro converterNodeParaLivro(JsonNode rootNode, String isbn) {
        Livro livro = new Livro();
        livro.setIsbn(isbn);

        if (rootNode.has(JSON_FIELD_TITLE)) {
            livro.setTitulo(rootNode.get(JSON_FIELD_TITLE).asText());
        }

        if (rootNode.has(JSON_FIELD_PUBLISH_DATE)) {
            livro.setDataPublicacao(rootNode.get(JSON_FIELD_PUBLISH_DATE).asText());
        }

        if (rootNode.has(JSON_FIELD_PUBLISHERS) && rootNode.get(JSON_FIELD_PUBLISHERS).isArray()) {
            JsonNode publishers = rootNode.get(JSON_FIELD_PUBLISHERS);
            if (!publishers.isEmpty()) {
                String nomeEditora = publishers.get(0).asText();
                livro.setEditora(new Editora(nomeEditora));
            }
        }

        return livro;
    }

    /**
     * Extrai um autor de um JsonNode
     */
    public Optional<Autor> extrairAutorDeNode(JsonNode authorNode) {
        if (authorNode == null) {
            return Optional.empty();
        }

        if (authorNode.has(JSON_FIELD_NAME)) {
            String nomeAutor = authorNode.get(JSON_FIELD_NAME).asText();
            return Optional.of(new Autor(nomeAutor));
        }

        return Optional.empty();
    }

    /**
     * Obtém o valor de um campo de texto do JsonNode
     */
    public String getTextFieldOrEmpty(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asText() : "";
    }

    /**
     * Obtém um nó de array do JsonNode
     */
    public JsonNode getArrayNodeOrEmpty(JsonNode node, String fieldName) {
        if (node.has(fieldName) && node.get(fieldName).isArray()) {
            return node.get(fieldName);
        }
        return objectMapper.createArrayNode();
    }

}
