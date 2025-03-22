package br.com.hadryan.app.view.components;

import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.view.MainFrame;
import br.com.hadryan.app.view.dialog.LivroDetailsDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Painel para pesquisa de livros no sistema.
 * Adaptado para tratar data de publicação como String.
 */
public class LivroPesquisaPainel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame janelaPrincipal;
    private final LivroController livroController;

    // Campos de pesquisa
    private JTextField tituloField;
    private JTextField isbnField;
    private JTextField autorField;
    private JTextField editoraField;
    private JTextField dataPublicacaoField;

    // Tabela de resultados
    private LivroTable resultadoTable;

    // Botões
    private JButton pesquisarButton;
    private JButton limparButton;
    private JButton visualizarButton;
    private JButton voltarButton;

    /**
     * Construtor do painel de pesquisa
     */
    public LivroPesquisaPainel(MainFrame janelaPrincipal, LivroController livroController) {
        this.janelaPrincipal = janelaPrincipal;
        this.livroController = livroController;

        initComponents();
        layoutComponents();
        setupListeners();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        // Campos de pesquisa
        tituloField = new JTextField(20);
        isbnField = new JTextField(15);
        autorField = new JTextField(20);
        editoraField = new JTextField(20);
        dataPublicacaoField = new JTextField(10);

        // Tabela de resultados
        resultadoTable = new LivroTable();

        // Botões
        pesquisarButton = new JButton("Pesquisar");
        limparButton = new JButton("Limpar");
        visualizarButton = new JButton("Visualizar Detalhes");
        voltarButton = new JButton("Voltar à Lista");

        // Inicialmente desabilita o botão de visualização
        visualizarButton.setEnabled(false);
    }

    /**
     * Configura o layout do painel
     */
    private void layoutComponents() {
        // Painel de pesquisa
        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(new JLabel("Título:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(tituloField, gbc);

        // ISBN
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("ISBN:"), gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(isbnField, gbc);

        // Autor
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("Autor:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(autorField, gbc);

        // Editora
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("Editora:"), gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(editoraField, gbc);

        // Data de Publicação
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("Data de Publicação:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(dataPublicacaoField, gbc);

        // Nota sobre formato de data - atualizada
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("(termo parcial, ex: 2023)"), gbc);

        // Botões de pesquisa
        JPanel searchButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButtonPanel.add(pesquisarButton);
        searchButtonPanel.add(limparButton);

        // Painel superior contendo formulário e botões de pesquisa
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(searchButtonPanel, BorderLayout.SOUTH);

        // Botões de resultados
        JPanel resultButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        resultButtonPanel.add(visualizarButton);
        resultButtonPanel.add(voltarButton);

        // Layout principal
        add(topPanel, BorderLayout.NORTH);
        add(resultadoTable, BorderLayout.CENTER);
        add(resultButtonPanel, BorderLayout.SOUTH);
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        // Listener de seleção da tabela
        resultadoTable.getTable().getSelectionModel().addListSelectionListener(e -> {
            visualizarButton.setEnabled(resultadoTable.getSelectedRow() != -1);
        });

        // Ação de duplo clique na tabela
        resultadoTable.setOnDoubleClickAction(this::visualizarLivro);

        // Ações dos botões
        pesquisarButton.addActionListener(e -> realizarPesquisa());
        limparButton.addActionListener(e -> limparFormulario());
        visualizarButton.addActionListener(e -> visualizarLivroSelecionado());
        voltarButton.addActionListener(e -> janelaPrincipal.mostrarPainel("LISTA"));
    }

    /**
     * Realiza a pesquisa com base nos critérios informados
     */
    private void realizarPesquisa() {
        // Cria objeto de filtro
        Livro filtro = new Livro();

        // Define título se informado
        if (!tituloField.getText().trim().isEmpty()) {
            filtro.setTitulo(tituloField.getText().trim());
        }

        // Define ISBN se informado
        if (!isbnField.getText().trim().isEmpty()) {
            filtro.setIsbn(isbnField.getText().trim());
        }

        // Define autor se informado
        if (!autorField.getText().trim().isEmpty()) {
            Autor autor = new Autor(autorField.getText().trim());
            filtro.adicionarAutor(autor);
        }

        // Define editora se informada
        if (!editoraField.getText().trim().isEmpty()) {
            Editora editora = new Editora(editoraField.getText().trim());
            filtro.setEditora(editora);
        }

        // Define data de publicação (como String) se informada
        if (!dataPublicacaoField.getText().trim().isEmpty()) {
            filtro.setDataPublicacao(dataPublicacaoField.getText().trim());
        }

        // Realiza a pesquisa
        try {
            resultadoTable.carregarDados(livroController.pesquisar(filtro));
            exibirQuantidadeResultados();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao realizar pesquisa: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exibe a quantidade de resultados encontrados
     */
    private void exibirQuantidadeResultados() {
        int quantidade = resultadoTable.getTableModel().getRowCount();
        JOptionPane.showMessageDialog(this,
                quantidade + " livro(s) encontrado(s).",
                "Resultado da Pesquisa",
                JOptionPane.INFORMATION_MESSAGE);
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
    }

    /**
     * Visualiza o livro selecionado na tabela
     */
    private void visualizarLivroSelecionado() {
        Livro livroSelecionado = resultadoTable.getLivroSelecionado();
        if (livroSelecionado != null) {
            visualizarLivro(livroSelecionado);
        }
    }

    /**
     * Visualiza um livro específico
     */
    private void visualizarLivro(Livro livro) {
        // Busca o livro completo pelo ID
        livroController.buscarPorId(livro.getId()).ifPresent(livroCompleto -> {
            // Abre o diálogo de detalhes
            LivroDetailsDialog dialog = new LivroDetailsDialog(janelaPrincipal, livroCompleto);
            dialog.setVisible(true);
        });
    }
}
