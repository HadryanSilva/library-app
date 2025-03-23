package br.com.hadryan.app.view.components.panel;

import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.view.MainFrame;
import br.com.hadryan.app.view.components.base.BaseCrudPanel;
import br.com.hadryan.app.view.components.base.BaseTable;
import br.com.hadryan.app.view.components.base.FormPanel;
import br.com.hadryan.app.view.components.dialog.LivroDetailsDialog;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;

/**
 * Painel refatorado para pesquisa de livros no sistema.
 */
public class LivroPesquisaPanel extends BaseCrudPanel {

    private static final long serialVersionUID = 1L;

    private static final String FIELD_TITULO = "titulo";
    private static final String FIELD_ISBN = "isbn";
    private static final String FIELD_AUTOR = "autor";
    private static final String FIELD_EDITORA = "editora";
    private static final String FIELD_DATA_PUBLICACAO = "dataPublicacao";

    private final MainFrame janelaPrincipal;
    private final LivroController livroController;

    private FormPanel searchFormPanel;
    private BaseTable<Livro> resultadoTable;
    private JButton visualizarButton;

    /**
     * Construtor do painel de pesquisa
     */
    public LivroPesquisaPanel(MainFrame janelaPrincipal, LivroController livroController) {
        this.janelaPrincipal = janelaPrincipal;
        this.livroController = livroController;

        initComponents();
        setupListeners();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        searchFormPanel = new FormPanel();

        searchFormPanel.addField("Título:", new JTextField(20), FIELD_TITULO);
        searchFormPanel.addField("ISBN:", new JTextField(15), FIELD_ISBN);
        searchFormPanel.addField("Autor:", new JTextField(20), FIELD_AUTOR);
        searchFormPanel.addField("Editora:", new JTextField(20), FIELD_EDITORA);
        searchFormPanel.addFieldWithComponent("Data de Publicação:",
                new JTextField(10), FIELD_DATA_PUBLICACAO,
                new JLabel("(termo parcial, ex: 2023)"));

        JPanel searchButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton pesquisarButton = new JButton("Pesquisar");
        JButton limparButton = new JButton("Limpar");

        pesquisarButton.addActionListener(e -> realizarPesquisa());
        limparButton.addActionListener(e -> limparFormulario());

        searchButtonPanel.add(pesquisarButton);
        searchButtonPanel.add(limparButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchFormPanel, BorderLayout.CENTER);
        topPanel.add(searchButtonPanel, BorderLayout.SOUTH);

        String[] colunas = {"ID", "Título", "Autores", "ISBN", "Editora", "Data de Publicação"};
        resultadoTable = new BaseTable<>(colunas);
        resultadoTable.setOnDoubleClickAction(this::visualizarLivro);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(resultadoTable, BorderLayout.CENTER);
        setMainComponent(mainPanel);

        visualizarButton = addActionButton("Visualizar Detalhes", e -> visualizarLivroSelecionado());
        addActionButton("Voltar à Lista", e -> janelaPrincipal.mostrarPainel("LISTA"));

        visualizarButton.setEnabled(false);
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        resultadoTable.addSelectionListener(livro -> {
            visualizarButton.setEnabled(livro != null);
        });
    }

    /**
     * Realiza a pesquisa com base nos critérios informados
     */
    private void realizarPesquisa() {
        Livro filtro = new Livro();

        String titulo = searchFormPanel.getTextFieldValue(FIELD_TITULO).trim();
        if (!titulo.isEmpty()) {
            filtro.setTitulo(titulo);
        }

        String isbn = searchFormPanel.getTextFieldValue(FIELD_ISBN).trim();
        if (!isbn.isEmpty()) {
            filtro.setIsbn(isbn);
        }

        String autor = searchFormPanel.getTextFieldValue(FIELD_AUTOR).trim();
        if (!autor.isEmpty()) {
            Autor autorObj = new Autor(autor);
            filtro.adicionarAutor(autorObj);
        }

        String editora = searchFormPanel.getTextFieldValue(FIELD_EDITORA).trim();
        if (!editora.isEmpty()) {
            Editora editoraObj = new Editora(editora);
            filtro.setEditora(editoraObj);
        }

        String dataPublicacao = searchFormPanel.getTextFieldValue(FIELD_DATA_PUBLICACAO).trim();
        if (!dataPublicacao.isEmpty()) {
            filtro.setDataPublicacao(dataPublicacao);
        }

        try {
            resultadoTable.setData(livroController.pesquisar(filtro), this::livroParaLinha);
            exibirQuantidadeResultados();
        } catch (Exception e) {
            showError("Erro ao realizar pesquisa: " + e.getMessage());
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
     * Exibe a quantidade de resultados encontrados
     */
    private void exibirQuantidadeResultados() {
        int quantidade = resultadoTable.getTableModel().getRowCount();
        showInfo(quantidade + " livro(s) encontrado(s).");
    }

    /**
     * Limpa o formulário de pesquisa e os resultados
     */
    private void limparFormulario() {
        searchFormPanel.setTextField(FIELD_TITULO, "");
        searchFormPanel.setTextField(FIELD_ISBN, "");
        searchFormPanel.setTextField(FIELD_AUTOR, "");
        searchFormPanel.setTextField(FIELD_EDITORA, "");
        searchFormPanel.setTextField(FIELD_DATA_PUBLICACAO, "");

        resultadoTable.getTableModel().setRowCount(0);
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
        // Não faz nada - a pesquisa é iniciada pelo usuário
    }
}
