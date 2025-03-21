package br.com.hadryan.app.service.strategy;

import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.model.entity.Publisher;
import br.com.hadryan.app.model.repository.AuthorRepository;
import br.com.hadryan.app.model.repository.PublisherRepository;
import br.com.hadryan.app.model.repository.RepositoryFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementação concreta da estratégia de importação para arquivos XML.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class XMLImportStrategy implements ImportStrategy {

    private static final Logger LOGGER = Logger.getLogger(XMLImportStrategy.class.getName());

    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;

    public XMLImportStrategy() {
        RepositoryFactory repositoryFactory = RepositoryFactory.getInstance();
        this.authorRepository = repositoryFactory.getAuthorRepository();
        this.publisherRepository = repositoryFactory.getPublisherRepository();
    }

    @Override
    public List<Book> importBooks(File file) throws IOException {
        List<Book> importedBooks = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Desativar processamento de DTD para evitar ataques XXE
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            // Verificar se a raiz é <books> ou <library>
            String rootName = document.getDocumentElement().getNodeName();
            if (!rootName.equals("books") && !rootName.equals("library")) {
                LOGGER.warning("XML inválido: elemento raiz deve ser <books> ou <library>.");
                return importedBooks;
            }

            // Processar nós de livros
            NodeList bookNodes = document.getElementsByTagName("book");
            for (int i = 0; i < bookNodes.getLength(); i++) {
                Element bookElement = (Element) bookNodes.item(i);
                Book book = parseBookElement(bookElement);

                if (book != null && book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                    importedBooks.add(book);
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            LOGGER.log(Level.SEVERE, "Erro ao fazer parse do XML: " + e.getMessage(), e);
            throw new IOException("Erro ao processar arquivo XML: " + e.getMessage(), e);
        }

        return importedBooks;
    }

    @Override
    public boolean supports(File file) {
        return file.getName().toLowerCase().endsWith(".xml");
    }

    /**
     * Faz o parse de um elemento XML de livro para um objeto Book
     *
     * @param bookElement Elemento XML do livro
     * @return Objeto Book ou null se o parse falhar
     */
    private Book parseBookElement(Element bookElement) {
        Book book = new Book();

        // ISBN (obrigatório)
        NodeList isbnNodes = bookElement.getElementsByTagName("isbn");
        if (isbnNodes.getLength() == 0) {
            return null; // ISBN é obrigatório
        }
        book.setIsbn(isbnNodes.item(0).getTextContent().trim());

        // Título
        NodeList titleNodes = bookElement.getElementsByTagName("title");
        if (titleNodes.getLength() > 0) {
            book.setTitle(titleNodes.item(0).getTextContent().trim());
        }

        // Data de publicação
        NodeList dateNodes = bookElement.getElementsByTagName("publicationDate");
        if (dateNodes.getLength() == 0) {
            dateNodes = bookElement.getElementsByTagName("publication_date");
        }

        if (dateNodes.getLength() > 0) {
            try {
                String dateStr = dateNodes.item(0).getTextContent().trim();
                LocalDate pubDate = LocalDate.parse(dateStr);
                book.setPublicationDate(pubDate);
            } catch (DateTimeParseException e) {
                LOGGER.log(Level.WARNING, "Erro ao converter data: " + dateNodes.item(0).getTextContent(), e);
            }
        }

        // Editora
        NodeList publisherNodes = bookElement.getElementsByTagName("publisher");
        if (publisherNodes.getLength() > 0) {
            String publisherName = publisherNodes.item(0).getTextContent().trim();
            if (!publisherName.isEmpty()) {
                Publisher publisher = publisherRepository.findByName(publisherName)
                        .orElse(new Publisher(publisherName));
                book.setPublisher(publisher);
            }
        }

        // Autores
        NodeList authorNodes = bookElement.getElementsByTagName("author");
        for (int j = 0; j < authorNodes.getLength(); j++) {
            String authorName = authorNodes.item(j).getTextContent().trim();
            if (!authorName.isEmpty()) {
                Author author = authorRepository.findByName(authorName)
                        .orElse(new Author(authorName));
                book.addAuthor(author);
            }
        }

        return book;
    }
}
