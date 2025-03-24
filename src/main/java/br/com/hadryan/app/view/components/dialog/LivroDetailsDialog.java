package br.com.hadryan.app.view.components.dialog;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.view.components.base.BaseDialog;
import br.com.hadryan.app.view.components.base.FormPanel;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;

/**
 * Diálogo refatorado para exibir detalhes de um livro.
 */
public class LivroDetailsDialog extends BaseDialog {

    private static final long serialVersionUID = 1L;

    // Constantes para os nomes dos campos
    private static final String FIELD_TITULO = "titulo";
    private static final String FIELD_ISBN = "isbn";
    private static final String FIELD_AUTORES = "autores";
    private static final String FIELD_EDITORA = "editora";
    private static final String FIELD_DATA_PUBLICACAO = "dataPublicacao";
    private static final String FIELD_LIVROS_SIMILARES = "livrosSimilares";

    private final Livro livro;
    private FormPanel formPanel;

    /**
     * Construtor do diálogo de detalhes
     */
    public LivroDetailsDialog(JFrame parent, Livro livro) {
        super(parent, "Detalhes do Livro");
        this.livro = livro;

        initComponents();
        preencherCampos();

        // Aumentando o tamanho do diálogo
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        formPanel = new FormPanel();

        JTextField tituloField = formPanel.addField("Título:", new JTextField(40), FIELD_TITULO);
        tituloField.setEditable(false);
        tituloField.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));

        JTextField isbnField = formPanel.addField("ISBN:", new JTextField(30), FIELD_ISBN);
        isbnField.setEditable(false);
        isbnField.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));

        JTextArea autoresArea = formPanel.addTextArea("Autores:", FIELD_AUTORES, 4, 40);
        autoresArea.setEditable(false);
        autoresArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));

        JTextField editoraField = formPanel.addField("Editora:", new JTextField(40), FIELD_EDITORA);
        editoraField.setEditable(false);
        editoraField.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));

        JTextField dataPublicacaoField = formPanel.addField("Data de Publicação:", new JTextField(25), FIELD_DATA_PUBLICACAO);
        dataPublicacaoField.setEditable(false);
        dataPublicacaoField.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));

        JTextArea livrosSimilaresArea = formPanel.addTextArea("Livros Similares:", FIELD_LIVROS_SIMILARES, 10, 40);
        livrosSimilaresArea.setEditable(false);
        livrosSimilaresArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));

        setMainComponent(formPanel);

        addButton("Fechar", e -> dispose());
    }

    /**
     * Preenche os campos com os dados do livro
     */
    private void preencherCampos() {
        // Preenche campos básicos
        formPanel.setTextField(FIELD_TITULO, livro.getTitulo());
        formPanel.setTextField(FIELD_ISBN, livro.getIsbn());

        if (livro.getEditora() != null) {
            formPanel.setTextField(FIELD_EDITORA, livro.getEditora().getNome());
        }

        // Exibe a data como String diretamente
        if (livro.getDataPublicacao() != null) {
            formPanel.setTextField(FIELD_DATA_PUBLICACAO, livro.getDataPublicacao());
        }

        // Autores como lista separada por vírgulas
        String autores = livro.getAutores().stream()
                .map(Autor::getNome)
                .collect(Collectors.joining(", "));
        formPanel.setTextField(FIELD_AUTORES, autores);

        // Livros similares no formato {isbn} - {name}
        String livrosSimilares = livro.getLivrosSimilares().stream()
                .map(l -> l.getIsbn() + " - " + l.getTitulo())
                .collect(Collectors.joining("\n"));
        formPanel.setTextField(FIELD_LIVROS_SIMILARES, livrosSimilares);
    }
}
