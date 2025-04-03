package br.com.hadryan.app.service.importacao;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementação concreta da estratégia de importação para arquivos XML.
 *
 * @author Hadryan Silva
 * @since 22-03-2025
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

        // Pré-processamento do arquivo para escapar caracteres especiais
        String xmlProcessado = preprocessarArquivoXml(arquivo);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();

            // Usa StringReader para o XML pré-processado
            try (StringReader reader = new StringReader(xmlProcessado)) {
                InputSource is = new InputSource(reader);
                Document document = builder.parse(is);
                document.getDocumentElement().normalize();
                String nomeRaiz = document.getDocumentElement().getNodeName();
                if (!nomeRaiz.equals("livros")) {
                    LOGGER.warning("XML inválido: elemento raiz deve ser <livros>.");
                    return livrosImportados;
                }

                NodeList nosLivros = document.getElementsByTagName("livro");
                int totalLivros = nosLivros.getLength();
                int livrosComIsbn = 0;

                for (int i = 0; i < totalLivros; i++) {
                    try {
                        Element elementoLivro = (Element) nosLivros.item(i);
                        Livro livro = processarElementoLivro(elementoLivro);

                        if (livro != null && livro.getIsbn() != null && !livro.getIsbn().trim().isEmpty()) {
                            livrosImportados.add(livro);
                            livrosComIsbn++;
                        } else {
                            LOGGER.warning("Livro sem ISBN ignorado na posição " + (i+1) + " do XML.");
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Erro ao processar livro no XML (índice " + i + "): " + e.getMessage(), e);
                    }
                }

                if (totalLivros > 0) {
                    LOGGER.info("Importação XML: " + livrosComIsbn + " de " + totalLivros
                            + " livros foram importados. "
                            + (totalLivros - livrosComIsbn) + " livros foram ignorados por falta de ISBN.");
                }
            }
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Erro de configuração do parser XML: " + e.getMessage(), e);
            throw new IOException("Erro ao processar arquivo XML: " + e.getMessage(), e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, "Erro de parsing XML: " + e.getMessage(), e);
            throw new IOException("Erro ao processar arquivo XML: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro inesperado ao processar XML: " + e.getMessage(), e);
            throw new IOException("Erro ao processar arquivo XML: " + e.getMessage(), e);
        }

        return livrosImportados;
    }

    /**
     * Pré-processa o arquivo XML para escapar caracteres especiais problemáticos.
     * @param arquivo Arquivo XML original
     * @return String contendo o XML pré-processado
     * @throws IOException Se ocorrer erro na leitura do arquivo
     */
    private String preprocessarArquivoXml(File arquivo) throws IOException {
        StringBuilder conteudo = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(arquivo.toPath()), StandardCharsets.UTF_8))) {

            String linha;
            while ((linha = reader.readLine()) != null) {
                conteudo.append(linha).append("\n");
            }
        }

        String xmlOriginal = conteudo.toString();

        Pattern ampersandPattern = Pattern.compile("&(?!(amp;|lt;|gt;|apos;|quot;))");
        Matcher matcher = ampersandPattern.matcher(xmlOriginal);

        return matcher.replaceAll("&amp;");
    }

    /**
     * Processa um elemento XML de livro para um objeto Livro
     * @return Objeto Livro se tiver ISBN, null caso contrário
     */
    private Livro processarElementoLivro(Element elementoLivro) {
        String isbn = getElementTextContent(elementoLivro, "isbn");
        if (isbn.isEmpty()) {
            return null;
        }
        isbn = isbn.trim();
        if (isbn.isEmpty()) {
            return null;
        }

        Livro livro = new Livro();
        livro.setIsbn(isbn);

        String titulo = getElementTextContent(elementoLivro, "titulo");
        if (!titulo.isEmpty()) {
            livro.setTitulo(titulo);
        } else {
            livro.setTitulo("Livro sem título (ISBN: " + isbn.substring(Math.max(0, isbn.length() - 6)) + ")");
        }

        String dataPublicacao = getElementTextContent(elementoLivro, "dataPublicacao");
        if (dataPublicacao.isEmpty()) {
            dataPublicacao = getElementTextContent(elementoLivro, "data_publicacao");
        }
        if (dataPublicacao.isEmpty()) {
            dataPublicacao = getElementTextContent(elementoLivro, "data");
        }
        if (!dataPublicacao.isEmpty()) {
            livro.setDataPublicacao(dataPublicacao);
        }

        String nomeEditora = getElementTextContent(elementoLivro, "editora");
        if (!nomeEditora.isEmpty()) {
            Editora editora = new Editora(nomeEditora);
            livro.setEditora(editora);
        }

        NodeList nosAutores = elementoLivro.getElementsByTagName("autor");
        if (nosAutores.getLength() > 0) {
            for (int j = 0; j < nosAutores.getLength(); j++) {
                String nomeAutor = nosAutores.item(j).getTextContent().trim();
                if (!nomeAutor.isEmpty()) {
                    Autor autor = new Autor(nomeAutor);
                    livro.adicionarAutor(autor);
                }
            }
        } else {
            String autoresText = getElementTextContent(elementoLivro, "autores");
            if (!autoresText.isEmpty()) {
                String[] autoresArray = autoresText.split("[,;]");
                for (String nomeAutor : autoresArray) {
                    String nome = nomeAutor.trim();
                    if (!nome.isEmpty()) {
                        Autor autor = new Autor(nome);
                        livro.adicionarAutor(autor);
                    }
                }
            }
        }

        return livro;
    }

    /**
     * Obtém o conteúdo de texto de um elemento
     * @param parent Elemento pai
     * @param tagName Nome da tag
     * @return Conteúdo de texto ou string vazia se não encontrar
     */
    private String getElementTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return "";
    }
}