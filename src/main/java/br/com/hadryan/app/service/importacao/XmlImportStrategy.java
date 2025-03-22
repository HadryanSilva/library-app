package br.com.hadryan.app.service.importacao;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementação concreta da estratégia de importação para arquivos XML.
 * Adaptada para tratar data de publicação como String.
 */
public class XmlImportStrategy implements ImportStrategy {

    private static final Logger LOGGER = Logger.getLogger(XmlImportStrategy.class.getName());

    @Override
    public boolean suporta(File arquivo) {
        return arquivo.getName().toLowerCase().endsWith(".xml");
    }

    @Override
    public List<Livro> importar(File arquivo) throws IOException {
        List<Livro> livrosImportados = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Desativar processamento de DTD para evitar ataques XXE
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(arquivo);
            document.getDocumentElement().normalize();

            // Verificar se a raiz é <livros> ou <biblioteca>
            String nomeRaiz = document.getDocumentElement().getNodeName();
            if (!nomeRaiz.equals("livros") && !nomeRaiz.equals("biblioteca")) {
                LOGGER.warning("XML inválido: elemento raiz deve ser <livros> ou <biblioteca>.");
                return livrosImportados;
            }

            // Processar nós de livros
            NodeList nosLivros = document.getElementsByTagName("livro");
            for (int i = 0; i < nosLivros.getLength(); i++) {
                Element elementoLivro = (Element) nosLivros.item(i);
                Livro livro = processarElementoLivro(elementoLivro);

                if (livro != null && livro.getIsbn() != null && !livro.getIsbn().isEmpty()) {
                    livrosImportados.add(livro);
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            LOGGER.log(Level.SEVERE, "Erro ao fazer parse do XML: " + e.getMessage(), e);
            throw new IOException("Erro ao processar arquivo XML: " + e.getMessage(), e);
        }

        return livrosImportados;
    }

    /**
     * Processa um elemento XML de livro para um objeto Livro
     */
    private Livro processarElementoLivro(Element elementoLivro) {
        Livro livro = new Livro();

        // ISBN (obrigatório)
        NodeList nosIsbn = elementoLivro.getElementsByTagName("isbn");
        if (nosIsbn.getLength() == 0) {
            return null; // ISBN é obrigatório
        }
        livro.setIsbn(nosIsbn.item(0).getTextContent().trim());

        // Título
        NodeList nosTitulo = elementoLivro.getElementsByTagName("titulo");
        if (nosTitulo.getLength() > 0) {
            livro.setTitulo(nosTitulo.item(0).getTextContent().trim());
        }

        // Data de publicação - armazena diretamente como String
        NodeList nosData = elementoLivro.getElementsByTagName("dataPublicacao");
        if (nosData.getLength() == 0) {
            nosData = elementoLivro.getElementsByTagName("data_publicacao");
        }

        if (nosData.getLength() > 0) {
            String dataStr = nosData.item(0).getTextContent().trim();
            if (!dataStr.isEmpty()) {
                livro.setDataPublicacao(dataStr);
            }
        }

        // Editora
        NodeList nosEditora = elementoLivro.getElementsByTagName("editora");
        if (nosEditora.getLength() > 0) {
            String nomeEditora = nosEditora.item(0).getTextContent().trim();
            if (!nomeEditora.isEmpty()) {
                Editora editora = new Editora(nomeEditora);
                livro.setEditora(editora);
            }
        }

        // Autores
        NodeList nosAutores = elementoLivro.getElementsByTagName("autor");
        for (int j = 0; j < nosAutores.getLength(); j++) {
            String nomeAutor = nosAutores.item(j).getTextContent().trim();
            if (!nomeAutor.isEmpty()) {
                Autor autor = new Autor(nomeAutor);
                livro.adicionarAutor(autor);
            }
        }

        return livro;
    }
}