package br.com.hadryan.app.service;

import br.com.hadryan.app.model.dto.AuthorDTO;
import br.com.hadryan.app.model.dto.BookDTO;
import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.model.entity.Publisher;
import br.com.hadryan.app.model.repository.AuthorRepository;
import br.com.hadryan.app.model.repository.PublisherRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OpenLibraryService {

    private static final Logger LOGGER = Logger.getLogger(OpenLibraryService.class.getName());
    private static final String API_URL = "https://openlibrary.org/isbn/";
    private static final String API_FORMAT = ".json";

    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final ObjectMapper objectMapper;

    public OpenLibraryService() {
        publisherRepository = new PublisherRepository();
        authorRepository = new AuthorRepository();

        // Configure ObjectMapper to ignore unknown properties
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Finds book data by ISBN from the OpenLibrary API
     *
     * @param isbn ISBN of the book to find
     * @return Optional containing the book, if found
     */
    public Optional<Book> findBookByIsbn(String isbn) {
        try {
            // Normalize the ISBN
            isbn = isbn.replaceAll("[^0-9X]", "");

            // Make the API request
            String jsonResponse = makeGetRequest(API_URL + isbn + API_FORMAT);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return Optional.empty();
            }

            // Parse the JSON response using Jackson
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Convert to domain model
            Book book = convertToBook(rootNode, isbn);
            return Optional.of(book);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error finding book by ISBN: " + isbn, e);
            return Optional.empty();
        }
    }

    /**
     * Executes a GET request to the API
     *
     * @param urlStr URL for the request
     * @return String containing the JSON response
     * @throws IOException in case of request error
     */
    private String makeGetRequest(String urlStr) throws IOException {
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
                LOGGER.log(Level.WARNING, "Non-OK response from API: " + responseCode);
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
     * Converts JSON data to a Book entity
     *
     * @param rootNode JsonNode containing the book data
     * @param isbn ISBN of the book
     * @return Book entity with data from JSON
     */
    private Book convertToBook(JsonNode rootNode, String isbn) {
        Book book = new Book();
        book.setIsbn(isbn);

        // Title
        if (rootNode.has("title")) {
            book.setTitle(rootNode.get("title").asText());
        }

        // Publication Date
        if (rootNode.has("publish_date")) {
            String publishDateStr = rootNode.get("publish_date").asText();
            LocalDate publishDate = LocalDate.parse(publishDateStr);
            book.setPublicationDate(publishDate);
        }

        // Publisher
        if (rootNode.has("publishers") && rootNode.get("publishers").isArray()) {
            JsonNode publishers = rootNode.get("publishers");
            if (!publishers.isEmpty()) {
                String publisherName = publishers.get(0).asText();
                Publisher publisher = publisherRepository.findByName(publisherName)
                        .orElse(new Publisher(publisherName));
                book.setPublisher(publisher);
            }
        }

        // Authors
        if (rootNode.has("authors") && rootNode.get("authors").isArray()) {
            for (JsonNode authorNode : rootNode.get("authors")) {
                try {
                    // Map JsonNode to AuthorDto using Jackson
                    AuthorDTO authorDto = objectMapper.treeToValue(authorNode, AuthorDTO.class);

                    if (authorDto.getName() != null && !authorDto.getName().isEmpty()) {
                        Author author = authorRepository.findByName(authorDto.getName())
                                .orElse(new Author(authorDto.getName()));
                        book.addAuthor(author);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error parsing author data", e);
                }
            }
        }

        return book;
    }

    /**
     * Alternative implementation using direct mapping to DTOs
     *
     * @param jsonResponse The JSON response string
     * @param isbn The ISBN of the book
     * @return Book entity with data from JSON
     */
    private Book parseJsonToBookViaDto(String jsonResponse, String isbn) {
        try {
            // Parse JSON directly to BookDto
            BookDTO bookDto = objectMapper.readValue(jsonResponse, BookDTO.class);

            // Convert DTO to entity
            Book book = new Book();
            book.setIsbn(isbn);
            book.setTitle(bookDto.getTitle());

            // Publication Date
            if (bookDto.getPublishDate() != null) {
                LocalDate publishDate = bookDto.getPublishDate();
                book.setPublicationDate(publishDate);
            }

            // Publisher
            if (bookDto.getPublishers() != null && !bookDto.getPublishers().isEmpty()) {
                String publisherName = bookDto.getPublishers().get(0).getName();
                Publisher publisher = publisherRepository.findByName(publisherName)
                        .orElse(new Publisher(publisherName));
                book.setPublisher(publisher);
            }

            // Authors
            if (bookDto.getAuthors() != null) {
                for (AuthorDTO authorDto : bookDto.getAuthors()) {
                    if (authorDto.getName() != null && !authorDto.getName().isEmpty()) {
                        Author author = authorRepository.findByName(authorDto.getName())
                                .orElse(new Author(authorDto.getName()));
                        book.addAuthor(author);
                    }
                }
            }

            return book;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error parsing JSON to BookDto", e);
            // Fallback to simple Book with ISBN only
            return new Book(null, isbn);
        }
    }
}
