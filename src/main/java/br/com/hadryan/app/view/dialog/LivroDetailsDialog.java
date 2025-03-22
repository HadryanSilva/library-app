package br.com.hadryan.app.view.dialog;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Livro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.stream.Collectors;

/**
 * Diálogo para exibir detalhes de um livro.
 * Adaptado para tratar data de publicação como String.
 */
public class LivroDetailsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final Livro livro;

    // Campos de exibição
    private JTextField tituloField;
    private JTextField isbnField;
    private JTextField editoraField;
    private JTextField dataPublicacaoField;
    private JTextArea autoresArea;
    private JTextArea livrosSimilaresArea;

    /**
     * Construtor do diálogo de detalhes
     */
    public LivroDetailsDialog(JFrame parent, Livro livro) {
        super(parent, "Detalhes do Livro", true);
        this.livro = livro;

        initComponents();
        layoutComponents();
        preencherCampos();

        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        // Campos de exibição - todos somente leitura
        tituloField = new JTextField(30);
        tituloField.setEditable(false);

        isbnField = new JTextField(20);
        isbnField.setEditable(false);

        editoraField = new JTextField(30);
        editoraField.setEditable(false);

        dataPublicacaoField = new JTextField(10);
        dataPublicacaoField.setEditable(false);

        autoresArea = new JTextArea(3, 30);
        autoresArea.setEditable(false);
        autoresArea.setLineWrap(true);
        autoresArea.setWrapStyleWord(true);

        livrosSimilaresArea = new JTextArea(3, 30);
        livrosSimilaresArea.setEditable(false);
        livrosSimilaresArea.setLineWrap(true);
        livrosSimilaresArea.setWrapStyleWord(true);
    }

    /**
     * Configura o layout do diálogo
     */
    private void layoutComponents() {
        // Painel de conteúdo
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Título:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(tituloField, gbc);

        // ISBN
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("ISBN:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(isbnField, gbc);

        // Autores
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Autores:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JScrollPane autoresScroll = new JScrollPane(autoresArea);
        contentPanel.add(autoresScroll, gbc);

        // Editora
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Editora:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(editoraField, gbc);

        // Data de Publicação
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Data de Publicação:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(dataPublicacaoField, gbc);

        // Livros Similares
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Livros Similares:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JScrollPane livrosSimilaresScroll = new JScrollPane(livrosSimilaresArea);
        contentPanel.add(livrosSimilaresScroll, gbc);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton fecharButton = new JButton("Fechar");
        fecharButton.addActionListener(e -> dispose());
        buttonPanel.add(fecharButton);

        // Layout principal
        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Preenche os campos com os dados do livro
     */
    private void preencherCampos() {
        // Preenche campos básicos
        tituloField.setText(livro.getTitulo());
        isbnField.setText(livro.getIsbn());

        if (livro.getEditora() != null) {
            editoraField.setText(livro.getEditora().getNome());
        }

        // Exibe a data como String diretamente
        if (livro.getDataPublicacao() != null) {
            dataPublicacaoField.setText(livro.getDataPublicacao());
        }

        // Autores como lista separada por vírgulas
        String autores = livro.getAutores().stream()
                .map(Autor::getNome)
                .collect(Collectors.joining(", "));
        autoresArea.setText(autores);

        // Livros similares como lista formatada
        String livrosSimilares = livro.getLivrosSimilares().stream()
                .map(l -> l.getTitulo() + " (ISBN: " + l.getIsbn() + ")")
                .collect(Collectors.joining("\n"));
        livrosSimilaresArea.setText(livrosSimilares);
    }
}
