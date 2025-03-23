package br.com.hadryan.app.view.components.dialog;

import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.view.components.LivrosSimilaresSelector;
import br.com.hadryan.app.view.components.base.BaseDialog;
import br.com.hadryan.app.view.components.base.FormPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Diálogo para criação e edição de livros
 */
public class LivroFormDialog extends BaseDialog {

    private static final String FIELD_TITULO = "titulo";
    private static final String FIELD_ISBN = "isbn";
    private static final String FIELD_EDITORA = "editora";
    private static final String FIELD_DATA_PUBLICACAO = "dataPublicacao";
    private static final String FIELD_AUTORES = "autores";
    private static final String FIELD_SIMILARES = "similares";

    private final Livro livro;
    private final LivroController livroController;
    private final boolean isModoEdicao;

    private FormPanel formPanel;
    private LivrosSimilaresSelector livrosSimilaresSelector;
    private JButton buscarButton;

    /**
     * Construtor do diálogo de formulário de livro
     */
    public LivroFormDialog(JFrame parent, Livro livro, LivroController livroController) {
        super(parent, (livro == null || livro.getId() == null) ? "Novo Livro" : "Editar Livro");

        this.livro = (livro == null) ? new Livro() : livro;
        this.livroController = livroController;
        this.isModoEdicao = (livro != null && livro.getId() != null);

        initComponents();
        setupListeners();
        preencherCampos();

        pack();
        setMinimumSize(new Dimension(550, 450));
        setLocationRelativeTo(parent);
    }

    /**
     * Inicializa os componentes do formulário
     */
    private void initComponents() {
        formPanel = new FormPanel();

        buscarButton = new JButton("Buscar por ISBN");

        formPanel.addFieldWithComponent("ISBN:",
                new JTextField(20), FIELD_ISBN, buscarButton);

        formPanel.addField("Título:",
                new JTextField(30), FIELD_TITULO);

        formPanel.addTextArea("Autores:", FIELD_AUTORES, 3, 30);

        formPanel.addField("Editora:",
                new JTextField(30), FIELD_EDITORA);

        JTextField dataField = formPanel.addFieldWithComponent("Data de Publicação:",
                new JTextField(10), FIELD_DATA_PUBLICACAO,
                new JLabel("(Qualquer formato)"));

        formPanel.addFullWidthComponent(new JLabel("Livros Similares:"), null);

        livrosSimilaresSelector = new LivrosSimilaresSelector(this, livroController);
        formPanel.addFullWidthComponent(livrosSimilaresSelector, FIELD_SIMILARES);

        setMainComponent(formPanel);

        addButton(isModoEdicao ? "Atualizar" : "Salvar", e -> salvarLivro());
        addButton("Cancelar", e -> cancel());
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        buscarButton.addActionListener(e -> buscarPorIsbn());
    }

    /**
     * Preenche os campos com os dados do livro
     */
    private void preencherCampos() {
        if (!isModoEdicao) {
            return;
        }

        formPanel.setTextField(FIELD_TITULO, livro.getTitulo());
        formPanel.setTextField(FIELD_ISBN, livro.getIsbn());

        if (livro.getEditora() != null) {
            formPanel.setTextField(FIELD_EDITORA, livro.getEditora().getNome());
        }

        if (livro.getDataPublicacao() != null) {
            formPanel.setTextField(FIELD_DATA_PUBLICACAO, livro.getDataPublicacao());
        }

        String autores = livro.getAutores().stream()
                .map(Autor::getNome)
                .collect(Collectors.joining(", "));
        formPanel.setTextField(FIELD_AUTORES, autores);

        livrosSimilaresSelector.setLivroAtual(livro);
    }

    /**
     * Salva o livro
     */
    private void salvarLivro() {
        String isbn = formPanel.getTextFieldValue(FIELD_ISBN).trim();
        String titulo = formPanel.getTextFieldValue(FIELD_TITULO).trim();

        if (isbn.isEmpty() || titulo.isEmpty()) {
            showError("ISBN e Título são campos obrigatórios.");
            return;
        }
        livro.setTitulo(titulo);
        livro.setIsbn(isbn);

        String dataPublicacao = formPanel.getTextFieldValue(FIELD_DATA_PUBLICACAO).trim();
        if (!dataPublicacao.isEmpty()) {
            livro.setDataPublicacao(dataPublicacao);
        }

        String nomeEditora = formPanel.getTextFieldValue(FIELD_EDITORA).trim();
        if (!nomeEditora.isEmpty()) {
            Editora editora = new Editora(nomeEditora);
            livro.setEditora(editora);
        }

        Set<Autor> autores = new HashSet<>();
        String textoAutores = formPanel.getTextFieldValue(FIELD_AUTORES).trim();
        if (!textoAutores.isEmpty()) {
            Arrays.stream(textoAutores.split(","))
                    .map(String::trim)
                    .filter(nome -> !nome.isEmpty())
                    .forEach(nome -> autores.add(new Autor(nome)));
        }
        livro.setAutores(autores);

        try {
            Livro livroSalvo = livroController.salvar(livro);

            List<String> isbnsSimilares = livrosSimilaresSelector.getIsbnsSelcionados();
            livroController.atualizarLivrosSimilares(livroSalvo, isbnsSimilares);

            confirm();
        } catch (Exception ex) {
            showError("Erro ao salvar livro: " + ex.getMessage());
        }
    }

    /**
     * Busca informações do livro pelo ISBN
     */
    private void buscarPorIsbn() {
        String isbn = formPanel.getTextFieldValue(FIELD_ISBN).trim();
        if (isbn.isEmpty()) {
            showError("Digite um ISBN para busca.");
            return;
        }

        buscarButton.setEnabled(false);
        buscarButton.setText("Buscando...");

        new SwingWorker<Livro, Void>() {
            @Override
            protected Livro doInBackground() {
                return livroController.buscarLivroPorIsbnApi(isbn)
                        .orElse(null);
            }

            @Override
            protected void done() {
                try {
                    Livro resultado = get();
                    if (resultado != null) {
                        formPanel.setTextField(FIELD_TITULO, resultado.getTitulo());

                        if (resultado.getEditora() != null) {
                            formPanel.setTextField(FIELD_EDITORA, resultado.getEditora().getNome());
                        }

                        if (resultado.getDataPublicacao() != null) {
                            formPanel.setTextField(FIELD_DATA_PUBLICACAO, resultado.getDataPublicacao());
                        }

                        String autores = resultado.getAutores().stream()
                                .map(Autor::getNome)
                                .collect(Collectors.joining(", "));
                        formPanel.setTextField(FIELD_AUTORES, autores);

                        showInfo("Livro encontrado e dados carregados.");
                    } else {
                        showInfo("Nenhum livro encontrado com ISBN: " + isbn);
                    }
                } catch (Exception ex) {
                    showError("Erro na busca por ISBN: " + ex.getMessage());
                } finally {
                    buscarButton.setEnabled(true);
                    buscarButton.setText("Buscar por ISBN");
                }
            }
        }.execute();
    }

    /**
     * Retorna o livro que foi editado ou criado
     */
    public Livro getLivro() {
        return livro;
    }
}
