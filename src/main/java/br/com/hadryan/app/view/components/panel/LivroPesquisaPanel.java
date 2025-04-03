package br.com.hadryan.app.view.components.panel;

import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.view.MainFrame;
import br.com.hadryan.app.view.components.base.BaseCrudPanel;
import br.com.hadryan.app.view.components.base.BaseTable;
import br.com.hadryan.app.view.components.dialog.LivroDetailsDialog;
import br.com.hadryan.app.view.components.validator.FormValidator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Painel para pesquisa de livros no sistema.
 *
 * @author Hadryan Silva
 * @since 22-03-2025
 */
public class LivroPesquisaPanel extends BaseCrudPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame janelaPrincipal;
    private final LivroController livroController;

    private JTextField tituloField;
    private JTextField isbnField;
    private JTextField autorField;
    private JTextField editoraField;
    private JTextField dataPublicacaoField;

    private BaseTable<Livro> resultadoTable;
    private JButton visualizarButton;
    private JLabel resultadosLabel;
    private JButton pesquisarButton;
    private FormValidator validator;

    /**
     * Construtor do painel de pesquisa
     */
    public LivroPesquisaPanel(MainFrame janelaPrincipal, LivroController livroController) {
        this.janelaPrincipal = janelaPrincipal;
        this.livroController = livroController;

        initComponents();
        setupValidation();
        setupListeners();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = createSearchPanel();
        JPanel resultsPanel = createResultsPanel();

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(resultsPanel, BorderLayout.CENTER);

        setMainComponent(mainPanel);

        visualizarButton = addActionButton("Visualizar Detalhes", e -> visualizarLivroSelecionado());
        addActionButton("Voltar à Lista", e -> janelaPrincipal.mostrarPainel("LISTA"));

        visualizarButton.setEnabled(false);
    }

    /**
     * Configura a validação do formulário
     */
    private void setupValidation() {
        validator = new FormValidator();

        // Adicionamos uma validação mínima: pelo menos um campo de pesquisa deve ser preenchido
        validator.addRequiredField(tituloField, "Pelo menos um campo de pesquisa");
    }

    /**
     * Cria o painel de pesquisa com layout aprimorado
     */
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Critérios de Pesquisa",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font(Font.DIALOG, Font.BOLD, 14)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel tituloLabel = new JLabel("Título:");
        tituloLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
        searchPanel.add(tituloLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.WEST;
        tituloField = new JTextField(20);
        searchPanel.add(tituloField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel isbnLabel = new JLabel("ISBN:");
        isbnLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
        searchPanel.add(isbnLabel, gbc);

        // Campo ISBN
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.WEST;
        isbnField = new JTextField(15);
        searchPanel.add(isbnField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel autorLabel = new JLabel("Autor:");
        autorLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
        searchPanel.add(autorLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.WEST;
        autorField = new JTextField(20);
        searchPanel.add(autorField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel editoraLabel = new JLabel("Editora:");
        editoraLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
        searchPanel.add(editoraLabel, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.WEST;
        editoraField = new JTextField(15);
        searchPanel.add(editoraField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel dataLabel = new JLabel("Data de Publicação:");
        dataLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
        searchPanel.add(dataLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.WEST;
        dataPublicacaoField = new JTextField(10);
        JPanel dataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dataPanel.add(dataPublicacaoField);
        dataPanel.add(new JLabel("   (termo parcial, ex: 2023)"));
        searchPanel.add(dataPanel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        pesquisarButton = new JButton("Pesquisar");
        pesquisarButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        pesquisarButton.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
        pesquisarButton.addActionListener(e -> realizarPesquisa());

        JButton limparButton = new JButton("Limpar");
        limparButton.setIcon(UIManager.getIcon("FileChooser.detailsViewIcon"));
        limparButton.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        limparButton.addActionListener(e -> limparFormulario());

        buttonPanel.add(limparButton);
        buttonPanel.add(pesquisarButton);
        searchPanel.add(buttonPanel, gbc);

        return searchPanel;
    }

    /**
     * Cria o painel de resultados
     */
    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout(5, 5));
        resultsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Resultados da Pesquisa",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font(Font.DIALOG, Font.BOLD, 14)));

        String[] colunas = {"ID", "Título", "Autores", "ISBN", "Editora", "Data de Publicação"};
        resultadoTable = new BaseTable<>(colunas);
        resultadoTable.setOnDoubleClickAction(this::visualizarLivro);

        resultadosLabel = new JLabel("Nenhum resultado");
        resultadosLabel.setFont(new Font(Font.DIALOG, Font.ITALIC, 12));
        resultadosLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

        resultsPanel.add(resultadosLabel, BorderLayout.NORTH);
        resultsPanel.add(resultadoTable, BorderLayout.CENTER);

        return resultsPanel;
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        resultadoTable.addSelectionListener(livro -> {
            visualizarButton.setEnabled(livro != null);
        });

        // Adicionamos listeners para os campos de texto para verificar se algum deles está preenchido
        DocumentListener documentListener = new DocumentListener();
        tituloField.getDocument().addDocumentListener(documentListener);
        isbnField.getDocument().addDocumentListener(documentListener);
        autorField.getDocument().addDocumentListener(documentListener);
        editoraField.getDocument().addDocumentListener(documentListener);
        dataPublicacaoField.getDocument().addDocumentListener(documentListener);
    }

    /**
     * Classe interna para monitorar alterações nos campos do formulário
     */
    private class DocumentListener implements javax.swing.event.DocumentListener {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            validateForm();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            validateForm();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            validateForm();
        }
    }

    /**
     * Valida o formulário verificando se pelo menos um campo está preenchido
     */
    private void validateForm() {
        boolean temCampoPreenchido = !tituloField.getText().trim().isEmpty() ||
                !isbnField.getText().trim().isEmpty() ||
                !autorField.getText().trim().isEmpty() ||
                !editoraField.getText().trim().isEmpty() ||
                !dataPublicacaoField.getText().trim().isEmpty();

        pesquisarButton.setEnabled(temCampoPreenchido);
    }

    /**
     * Realiza a pesquisa com base nos critérios informados
     */
    private void realizarPesquisa() {
        // Validamos se há pelo menos um campo preenchido
        boolean temCriterio = !tituloField.getText().trim().isEmpty() ||
                !isbnField.getText().trim().isEmpty() ||
                !autorField.getText().trim().isEmpty() ||
                !editoraField.getText().trim().isEmpty() ||
                !dataPublicacaoField.getText().trim().isEmpty();

        if (!temCriterio) {
            showError("Informe pelo menos um critério de pesquisa.");
            return;
        }

        Livro filtro = new Livro();

        String titulo = tituloField.getText().trim();
        if (!titulo.isEmpty()) {
            filtro.setTitulo(titulo);
        }

        String isbn = isbnField.getText().trim();
        if (!isbn.isEmpty()) {
            filtro.setIsbn(isbn);
        }

        String autor = autorField.getText().trim();
        if (!autor.isEmpty()) {
            Autor autorObj = new Autor(autor);
            filtro.adicionarAutor(autorObj);
        }

        String editora = editoraField.getText().trim();
        if (!editora.isEmpty()) {
            Editora editoraObj = new Editora(editora);
            filtro.setEditora(editoraObj);
        }

        String dataPublicacao = dataPublicacaoField.getText().trim();
        if (!dataPublicacao.isEmpty()) {
            filtro.setDataPublicacao(dataPublicacao);
        }

        try {
            List<Livro> resultados = livroController.pesquisar(filtro);
            resultadoTable.setData(resultados, this::livroParaLinha);
            atualizarLabelResultados(resultados.size());
        } catch (Exception e) {
            showError("Erro ao realizar pesquisa: " + e.getMessage());
        }
    }

    /**
     * Atualiza o label com a quantidade de resultados
     */
    private void atualizarLabelResultados(int quantidade) {
        if (quantidade == 0) {
            resultadosLabel.setText("Nenhum resultado encontrado");
            resultadosLabel.setForeground(Color.RED);
        } else {
            resultadosLabel.setText(quantidade + " livro(s) encontrado(s)");
            resultadosLabel.setForeground(new Color(0, 100, 0)); // Verde escuro
        }
    }

    /**
     * Converte um livro para uma linha da tabela
     */
    private Object[] livroParaLinha(Livro livro) {
        String autores = livro.getAutores().stream()
                .map(Autor::getNome)
                .collect(Collectors.joining(", "));

        return new Object[] {
                livro.getId(),
                livro.getTitulo(),
                autores,
                livro.getIsbn(),
                livro.getEditora() != null ? livro.getEditora().getNome() : "",
                livro.getDataPublicacao() != null ? livro.getDataPublicacao() : ""
        };
    }

    /**
     * Limpa o formulário de pesquisa e os resultados
     */
    private void limparFormulario() {
        tituloField.setText("");
        isbnField.setText("");
        autorField.setText("");
        editoraField.setText("");
        dataPublicacaoField.setText("");

        resultadoTable.getTableModel().setRowCount(0);
        resultadosLabel.setText("Nenhum resultado");
        resultadosLabel.setForeground(Color.BLACK);

        // Resetamos qualquer estado de erro
        validator.clearErrors();

        // Atualizamos o estado do botão de pesquisa
        validateForm();
    }

    /**
     * Visualiza o livro selecionado na tabela
     */
    private void visualizarLivroSelecionado() {
        Livro livroSelecionado = resultadoTable.getSelectedItem();
        if (livroSelecionado != null) {
            visualizarLivro(livroSelecionado);
        }
    }

    /**
     * Visualiza um livro específico
     */
    private void visualizarLivro(Livro livro) {
        livroController.buscarPorId(livro.getId()).ifPresent(livroCompleto -> {
            LivroDetailsDialog dialog = new LivroDetailsDialog(janelaPrincipal, livroCompleto);
            dialog.setVisible(true);
        });
    }

    /**
     * Implementação do método abstrato da classe base
     */
    @Override
    public void updateData() {
        // Na tela de pesquisa, não precisamos carregar dados automaticamente
    }
}
