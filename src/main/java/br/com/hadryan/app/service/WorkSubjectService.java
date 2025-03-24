package br.com.hadryan.app.service;

import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.service.http.HttpClientWrapper;
import br.com.hadryan.app.service.json.OpenLibraryJsonParser;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Serviço responsável por buscar livros relacionados por assuntos (subjects)
 * utilizando a API do OpenLibrary.
 *
 * @author Hadryan Silva
 * @since 24-03-2025
 */
public class WorkSubjectService {

    private static final Logger LOGGER = Logger.getLogger(WorkSubjectService.class.getName());
    private static final String API_WORK_URL = "https://openlibrary.org";
    private static final String API_FORMAT = ".json";
    private static final int SEARCH_TIMEOUT_SECONDS = 10;

    private final HttpClientWrapper httpClient;
    private final OpenLibraryJsonParser jsonParser;
    private final OpenLibraryService openLibraryService;

    /**
     * Construtor que inicializa as dependências
     */
    public WorkSubjectService(OpenLibraryService openLibraryService) {
        this.openLibraryService = openLibraryService;
        this.httpClient = new HttpClientWrapper();
        this.httpClient.setUseCache(true);
        this.httpClient.setTimeouts(10000, 10000);
        this.jsonParser = new OpenLibraryJsonParser();
    }

    /**
     * Busca workKey de um livro pelo ISBN
     */
    public Optional<String> buscarWorkKey(String isbn) {
        try {
            String jsonResponse = httpClient.fazerRequisicaoGet(API_WORK_URL + "/isbn/" + isbn + API_FORMAT);
            Optional<JsonNode> optRootNode = jsonParser.parseJsonToNode(jsonResponse);
            if (!optRootNode.isPresent()) {
                return Optional.empty();
            }

            JsonNode rootNode = optRootNode.get();
            JsonNode worksNode = jsonParser.getArrayNodeOrEmpty(rootNode, "works");
            if (!worksNode.isEmpty() && worksNode.get(0).has("key")) {
                return Optional.of(worksNode.get(0).get("key").asText());
            }

            return Optional.empty();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar work key para ISBN: " + isbn, e);
            return Optional.empty();
        }
    }

    /**
     * Busca livros relacionados diretamente pelo endpoint /works
     * usando todas as informações disponíveis no objeto work
     */
    public List<Livro> buscarLivrosRelacionados(String isbn, int maxResultados) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Collections.emptyList();
        }

        LOGGER.log(Level.INFO, "Iniciando busca de livros relacionados para ISBN: " + isbn);
        Optional<String> workKeyOpt = buscarWorkKey(isbn);
        if (!workKeyOpt.isPresent()) {
            LOGGER.log(Level.INFO, "Não foi possível encontrar workKey para o ISBN: " + isbn);
            return Collections.emptyList();
        }

        String workKey = workKeyOpt.get();
        LOGGER.log(Level.INFO, "WorkKey encontrado: " + workKey);

        try {
            String jsonResponse = httpClient.fazerRequisicaoGet(API_WORK_URL + workKey + API_FORMAT);
            Optional<JsonNode> optRootNode = jsonParser.parseJsonToNode(jsonResponse);
            if (!optRootNode.isPresent()) {
                LOGGER.log(Level.WARNING, "Não foi possível obter detalhes do work: " + workKey);
                return Collections.emptyList();
            }

            JsonNode rootNode = optRootNode.get();
            Set<Livro> livrosRelacionados;
            List<Livro> livrosDaSerie = buscarLivrosDaMesmaSerie(rootNode, isbn);
            LOGGER.log(Level.INFO, "Encontrados " + livrosDaSerie.size() + " livros da mesma série");
            livrosRelacionados = new LinkedHashSet<>(livrosDaSerie);
            if (livrosRelacionados.size() < maxResultados && rootNode.has("authors")) {
                List<Livro> livrosDoAutor = buscarLivrosDoMesmoAutor(rootNode, isbn);
                LOGGER.log(Level.INFO, "Encontrados " + livrosDoAutor.size() + " livros do mesmo autor");
                livrosRelacionados.addAll(livrosDoAutor);
            }

            if (livrosRelacionados.size() < maxResultados) {
                List<Livro> obrasRelacionadas = buscarObrasRelacionadas(rootNode, isbn);
                LOGGER.log(Level.INFO, "Encontradas " + obrasRelacionadas.size() + " obras relacionadas diretamente");
                livrosRelacionados.addAll(obrasRelacionadas);
            }

            if (livrosRelacionados.size() < maxResultados) {
                int remaining = maxResultados - livrosRelacionados.size();
                List<Livro> livrosPorSubjects = buscarLivrosPorSubjects(rootNode, isbn, remaining);
                LOGGER.log(Level.INFO, "Encontrados " + livrosPorSubjects.size() + " livros por subjects similares");
                livrosRelacionados.addAll(livrosPorSubjects);
            }

            List<Livro> resultado = livrosRelacionados.stream()
                    .limit(maxResultados)
                    .collect(Collectors.toList());

            LOGGER.log(Level.INFO, "Total de livros relacionados encontrados: " + resultado.size());
            return resultado;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar livros relacionados para work: " + workKey, e);
            return Collections.emptyList();
        }
    }

    /**
     * Busca livros do mesmo autor
     */
    private List<Livro> buscarLivrosDoMesmoAutor(JsonNode workNode, String isbnOriginal) {
        List<Livro> resultado = new ArrayList<>();
        try {
            JsonNode authorsNode = jsonParser.getArrayNodeOrEmpty(workNode, "authors");
            if (authorsNode.isEmpty()) {
                return resultado;
            }

            JsonNode firstAuthor = authorsNode.get(0);
            String authorKey = null;
            if (firstAuthor.has("author") && firstAuthor.get("author").has("key")) {
                authorKey = firstAuthor.get("author").get("key").asText();
            } else if (firstAuthor.has("key")) {
                authorKey = firstAuthor.get("key").asText();
            }

            if (authorKey == null) {
                return resultado;
            }

            String authorWorksUrl = API_WORK_URL + authorKey + "/works.json?limit=5";
            String jsonResponse = httpClient.fazerRequisicaoGet(authorWorksUrl);
            Optional<JsonNode> optAuthorWorks = jsonParser.parseJsonToNode(jsonResponse);
            if (!optAuthorWorks.isPresent()) {
                return resultado;
            }

            JsonNode authorWorksNode = optAuthorWorks.get();
            JsonNode entriesNode = jsonParser.getArrayNodeOrEmpty(authorWorksNode, "entries");
            for (JsonNode entry : entriesNode) {
                if (entry.has("key")) {
                    String workKey = entry.get("key").asText();
                    Optional<Livro> livroOpt = buscarLivroProWorkKey(workKey, isbnOriginal);
                    if (livroOpt.isPresent()) {
                        resultado.add(livroOpt.get());

                        if (resultado.size() >= 2) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar livros do mesmo autor", e);
        }
        return resultado;
    }

    /**
     * Busca livros da mesma série
     */
    private List<Livro> buscarLivrosDaMesmaSerie(JsonNode workNode, String isbnOriginal) {
        List<Livro> resultado = new ArrayList<>();
        try {
            if (workNode.has("series")) {
                JsonNode seriesNode = workNode.get("series");
                if (seriesNode.isArray()) {
                    for (JsonNode serie : seriesNode) {
                        String serieUrl = obterLinkDaSerie(serie);
                        if (serieUrl != null) {
                            resultado.addAll(buscarLivrosNaSerie(serieUrl, isbnOriginal));
                        }
                    }
                } else {
                    String serieUrl = obterLinkDaSerie(seriesNode);
                    if (serieUrl != null) {
                        resultado.addAll(buscarLivrosNaSerie(serieUrl, isbnOriginal));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar livros da mesma série", e);
        }
        return resultado;
    }

    /**
     * Obtém o link da série a partir do nó
     */
    private String obterLinkDaSerie(JsonNode serieNode) {
        if (serieNode.isTextual()) {
            return serieNode.asText();
        } else if (serieNode.has("url")) {
            return serieNode.get("url").asText();
        } else if (serieNode.has("key")) {
            return serieNode.get("key").asText();
        }
        return null;
    }

    /**
     * Busca livros em uma série específica
     */
    private List<Livro> buscarLivrosNaSerie(String serieUrl, String isbnOriginal) {
        List<Livro> resultado = new ArrayList<>();

        try {
            if (!serieUrl.startsWith("http")) {
                serieUrl = API_WORK_URL + serieUrl + API_FORMAT;
            }

            String jsonResponse = httpClient.fazerRequisicaoGet(serieUrl);
            Optional<JsonNode> optSerieNode = jsonParser.parseJsonToNode(jsonResponse);
            if (!optSerieNode.isPresent()) {
                return resultado;
            }

            JsonNode serieNode = optSerieNode.get();
            JsonNode worksNode = jsonParser.getArrayNodeOrEmpty(serieNode, "works");
            if (worksNode.isEmpty() && serieNode.has("entries")) {
                worksNode = serieNode.get("entries");
            }

            for (JsonNode workEntry : worksNode) {
                if (workEntry.has("key")) {
                    String workKey = workEntry.get("key").asText();
                    Optional<Livro> livroOpt = buscarLivroProWorkKey(workKey, isbnOriginal);
                    if (livroOpt.isPresent()) {
                        resultado.add(livroOpt.get());
                        if (resultado.size() >= 3) {
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar livros na série: " + serieUrl, e);
        }

        return resultado;
    }

    /**
     * Busca obras relacionadas diretamente listadas no nó work
     */
    private List<Livro> buscarObrasRelacionadas(JsonNode workNode, String isbnOriginal) {
        List<Livro> resultado = new ArrayList<>();

        try {
            if (workNode.has("links")) {
                JsonNode linksNode = workNode.get("links");
                for (JsonNode link : linksNode) {
                    if (link.has("title") && link.has("url")) {
                        String linkType = link.has("type") ? link.get("type").asText() : "";
                        if ("related".equalsIgnoreCase(linkType)) {
                            String url = link.get("url").asText();
                            if (url.contains("/works/")) {
                                String workKey = extrairWorkKeyDeUrl(url);
                                Optional<Livro> livroOpt = buscarLivroProWorkKey(workKey, isbnOriginal);
                                livroOpt.ifPresent(resultado::add);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar obras relacionadas", e);
        }

        return resultado;
    }

    /**
     * Extrai o workKey de uma URL completa
     */
    private String extrairWorkKeyDeUrl(String url) {
        int worksIndex = url.indexOf("/works/");
        if (worksIndex != -1) {
            String workPath = url.substring(worksIndex);
            int queryIndex = workPath.indexOf('?');
            if (queryIndex != -1) {
                workPath = workPath.substring(0, queryIndex);
            }
            return workPath;
        }
        return url;
    }

    /**
     * Busca livros por subjects do work, usando os dados embutidos na resposta
     */
    private List<Livro> buscarLivrosPorSubjects(JsonNode workNode, String isbnOriginal, int maxResultados) {
        Set<Livro> resultado = new HashSet<>();

        try {
            if (workNode.has("subject_people")) {
                resultado.addAll(buscarLivrosPorTermosRelacionados(workNode, "subject_people", isbnOriginal));
            }

            if (workNode.has("subject_places")) {
                resultado.addAll(buscarLivrosPorTermosRelacionados(workNode, "subject_places", isbnOriginal));
            }

            if (workNode.has("subject_times")) {
                resultado.addAll(buscarLivrosPorTermosRelacionados(workNode, "subject_times", isbnOriginal));
            }
            if (resultado.size() < maxResultados) {
                JsonNode subjectsNode = jsonParser.getArrayNodeOrEmpty(workNode, "subjects");
                List<String> subjects = new ArrayList<>();

                for (JsonNode subject : subjectsNode) {
                    if (subject.isTextual()) {
                        subjects.add(subject.asText());
                    }
                }

                List<String> filteredSubjects = subjects.stream()
                        .filter(s -> s.length() > 3)
                        .collect(Collectors.toList());

                if (filteredSubjects.isEmpty() && !subjects.isEmpty()) {
                    filteredSubjects = subjects;
                }

                List<String> searchSubjects = filteredSubjects.stream()
                        .limit(3)
                        .collect(Collectors.toList());

                if (!searchSubjects.isEmpty()) {
                    if (workNode.has("subject_works")) {
                        JsonNode subjectWorksNode = workNode.get("subject_works");
                        for (JsonNode subjectWork : subjectWorksNode) {
                            if (subjectWork.has("key")) {
                                String workKey = subjectWork.get("key").asText();
                                Optional<Livro> livroOpt = buscarLivroProWorkKey(workKey, isbnOriginal);
                                if (livroOpt.isPresent()) {
                                    resultado.add(livroOpt.get());

                                    if (resultado.size() >= maxResultados) {
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (resultado.size() < maxResultados) {
                        ExecutorService executor = Executors.newFixedThreadPool(Math.min(searchSubjects.size(), 3));
                        List<Future<List<Livro>>> futures = new ArrayList<>();
                        for (String subject : searchSubjects) {
                            futures.add(executor.submit(() -> buscarLivrosPorSubject(subject, isbnOriginal)));
                        }

                        try {
                            executor.shutdown();
                            boolean completed = executor.awaitTermination(SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                            if (!completed) {
                                executor.shutdownNow();
                            }

                            for (Future<List<Livro>> future : futures) {
                                if (future.isDone()) {
                                    try {
                                        resultado.addAll(future.get());
                                        if (resultado.size() >= maxResultados) {
                                            break;
                                        }
                                    } catch (ExecutionException e) {
                                        LOGGER.log(Level.WARNING, "Erro ao processar busca por subject", e);
                                    }
                                }
                            }
                        } catch (InterruptedException e) {
                            LOGGER.log(Level.WARNING, "Busca por subjects interrompida", e);
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar livros por subjects", e);
        }
        return resultado.stream()
                .limit(maxResultados)
                .collect(Collectors.toList());
    }

    /**
     * Busca livros relacionados pelos termos especializados do work
     * (subject_people, subject_places, subject_times)
     */
    private List<Livro> buscarLivrosPorTermosRelacionados(JsonNode workNode, String propertyName, String isbnOriginal) {
        List<Livro> resultado = new ArrayList<>();

        try {
            JsonNode termsNode = jsonParser.getArrayNodeOrEmpty(workNode, propertyName);
            if (!termsNode.isEmpty()) {
                List<String> termsToSearch = new ArrayList<>();
                for (int i = 0; i < Math.min(2, termsNode.size()); i++) {
                    if (termsNode.get(i).isTextual()) {
                        termsToSearch.add(termsNode.get(i).asText());
                    }
                }
                for (String term : termsToSearch) {
                    String url = API_WORK_URL + "/search.json?q=" + propertyName + ":\""
                            + java.net.URLEncoder.encode(term, "UTF-8") + "\"&limit=5";

                    String jsonResponse = httpClient.fazerRequisicaoGet(url);
                    Optional<JsonNode> optRootNode = jsonParser.parseJsonToNode(jsonResponse);

                    if (optRootNode.isPresent()) {
                        JsonNode rootNode = optRootNode.get();
                        JsonNode docsNode = jsonParser.getArrayNodeOrEmpty(rootNode, "docs");
                        for (JsonNode doc : docsNode) {
                            if (doc.has("key")) {
                                String key = doc.get("key").asText();
                                if (key.startsWith("/works/")) {
                                    Optional<Livro> livroOpt = buscarLivroProWorkKey(key, isbnOriginal);
                                    if (livroOpt.isPresent()) {
                                        resultado.add(livroOpt.get());

                                        if (resultado.size() >= 2) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar livros por " + propertyName, e);
        }

        return resultado;
    }

    /**
     * Busca livros por um subject específico usando a resposta do workNode original
     * para buscar outras obras relevantes
     */
    private List<Livro> buscarLivrosPorSubject(String subject, String isbnOriginal) {
        List<Livro> resultado = new ArrayList<>();
        try {
            String relatedUrl = API_WORK_URL + "/related/inside.json?subject="
                    + java.net.URLEncoder.encode(subject, "UTF-8") + "&limit=10";

            String jsonResponse = httpClient.fazerRequisicaoGet(relatedUrl);
            Optional<JsonNode> optRootNode = jsonParser.parseJsonToNode(jsonResponse);

            if (!optRootNode.isPresent()) {
                return resultado;
            }

            JsonNode rootNode = optRootNode.get();
            JsonNode worksNode = jsonParser.getArrayNodeOrEmpty(rootNode, "works");

            if (worksNode.isEmpty() && rootNode.has("matches")) {
                worksNode = rootNode.get("matches");
            }

            for (JsonNode workNode : worksNode) {
                if (workNode.has("key")) {
                    String workKey = workNode.get("key").asText();
                    Optional<Livro> livroOpt = buscarLivroProWorkKey(workKey, isbnOriginal);
                    if (livroOpt.isPresent()) {
                        resultado.add(livroOpt.get());
                        if (resultado.size() >= 2) {
                            break;
                        }
                    }
                }
            }

            if (resultado.isEmpty()) {
                String url = API_WORK_URL + "/search.json?q=subject:\""
                        + java.net.URLEncoder.encode(subject, "UTF-8") + "\"&limit=5";
                jsonResponse = httpClient.fazerRequisicaoGet(url);
                optRootNode = jsonParser.parseJsonToNode(jsonResponse);
                if (optRootNode.isPresent()) {
                    rootNode = optRootNode.get();
                    JsonNode docsNode = jsonParser.getArrayNodeOrEmpty(rootNode, "docs");

                    for (JsonNode doc : docsNode) {
                        if (doc.has("key")) {
                            String key = doc.get("key").asText();
                            if (key.startsWith("/works/")) {
                                Optional<Livro> livroOpt = buscarLivroProWorkKey(key, isbnOriginal);
                                if (livroOpt.isPresent()) {
                                    resultado.add(livroOpt.get());

                                    if (resultado.size() >= 2) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar livros por subject: " + subject, e);
        }

        return resultado;
    }

    /**
     * Extrai o ISBN de uma edição
     */
    private String extrairIsbnDaEdicao(JsonNode edicaoNode) {
        if (edicaoNode.has("isbn_13")) {
            JsonNode isbn13Node = edicaoNode.get("isbn_13");
            if (isbn13Node.isArray() && !isbn13Node.isEmpty()) {
                return isbn13Node.get(0).asText();
            }
        }

        if (edicaoNode.has("isbn_10")) {
            JsonNode isbn10Node = edicaoNode.get("isbn_10");
            if (isbn10Node.isArray() && !isbn10Node.isEmpty()) {
                return isbn10Node.get(0).asText();
            }
        }

        return null;
    }

    /**
     * Busca um livro pelo workKey
     */
    private Optional<Livro> buscarLivroProWorkKey(String workKey, String isbnOriginal) {
        try {
            if (!workKey.startsWith("/works/")) {
                workKey = "/works/" + workKey;
            }

            String jsonResponse = httpClient.fazerRequisicaoGet(API_WORK_URL + workKey + API_FORMAT);
            Optional<JsonNode> optWorkNode = jsonParser.parseJsonToNode(jsonResponse);
            if (!optWorkNode.isPresent()) {
                return Optional.empty();
            }

            JsonNode workNode = optWorkNode.get();
            if (!workNode.has("title")) {
                return Optional.empty();
            }

            String editionsUrl = API_WORK_URL + workKey + "/editions.json?limit=1";
            String editionsResponse = httpClient.fazerRequisicaoGet(editionsUrl);
            Optional<JsonNode> optEditionsNode = jsonParser.parseJsonToNode(editionsResponse);
            if (!optEditionsNode.isPresent()) {
                return Optional.empty();
            }

            JsonNode editionsNode = optEditionsNode.get();
            JsonNode entries = jsonParser.getArrayNodeOrEmpty(editionsNode, "entries");
            if (entries.isEmpty()) {
                return Optional.empty();
            }

            JsonNode firstEdition = entries.get(0);
            String isbn = extrairIsbnDaEdicao(firstEdition);
            if (isbn == null || isbn.equals(isbnOriginal)) {
                return Optional.empty();
            }
            return openLibraryService.buscarLivroPorIsbn(isbn);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao buscar livro pelo workKey: " + workKey, e);
            return Optional.empty();
        }
    }
}
