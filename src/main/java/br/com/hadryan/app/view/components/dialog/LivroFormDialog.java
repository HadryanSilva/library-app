package br.com.hadryan.app.view.components.dialog;

import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.view.components.LivrosSimilaresSelector;
import br.com.hadryan.app.view.components.base.BaseDialog;
import br.com.hadryan.app.view.components.base.FormPanel;
import br.com.hadryan.app.view.components.validator.FormValidator;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Diálogo para criação e edição de livros
 *
 * @author Hadryan Silva
 * @since 23-03-2025
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
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private FormValidator validator;

    /**
     * Construtor do diálogo de formulário de livro
     */
    public LivroFormDialog(JFrame parent, Livro livro, LivroController livroController) {
        super(parent, (livro == null || livro.getId() == null) ? "Novo Livro" : "Editar Livro");

        this.livro = (livro == null) ? new Livro() : livro;
        this.livroController = livroController;
        this.isModoEdicao = (livro != null && livro.getId() != null);

        initComponents();
        setupValidation();
        setupListeners();
        preencherCampos();

        // Aumentando o tamanho do diálogo
        setSize(800, 700);
        setMinimumSize(new Dimension(700, 600));
        setLocationRelativeTo(parent);
    }

    /**
     * Inicializa os componentes do formulário
     */
    private void initComponents() {
        // Vamos usar um layout diferente para o conteúdo principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Inicializa o FormPanel com um layout que preenche melhor o espaço
        formPanel = new FormPanel();
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Campo ISBN com botão de busca
        buscarButton = new JButton("Buscar por ISBN");
        JPanel isbnPanel = new JPanel(new BorderLayout(5, 0));
        JTextField isbnField = new JTextField(20);
        isbnPanel.add(isbnField, BorderLayout.CENTER);
        isbnPanel.add(buscarButton, BorderLayout.EAST);
        formPanel.addFieldWithComponent("ISBN:", isbnField, FIELD_ISBN, buscarButton);

        // Campos de texto com fontes maiores
        JTextField tituloField = new JTextField(40);
        tituloField.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        formPanel.addField("Título:", tituloField, FIELD_TITULO);

        JTextArea autoresArea = new JTextArea(3, 40);
        autoresArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        autoresArea.setLineWrap(true);
        autoresArea.setWrapStyleWord(true);
        formPanel.addTextArea("Autores:", FIELD_AUTORES, 3, 40);

        JTextField editoraField = new JTextField(40);
        editoraField.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        formPanel.addField("Editora:", editoraField, FIELD_EDITORA);

        JTextField dataField = new JTextField(15);
        dataField.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        JLabel formatoLabel = new JLabel("(Qualquer formato)");
        formPanel.addFieldWithComponent("Data de Publicação:", dataField, FIELD_DATA_PUBLICACAO, formatoLabel);

        JLabel similaresLabel = new JLabel("Livros Similares:");
        similaresLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        formPanel.addFullWidthComponent(similaresLabel, null);

        // Painel para livros similares com indicador de progresso
        JPanel similaresPanel = new JPanel(new BorderLayout());

        // Componente seletor de livros similares
        livrosSimilaresSelector = new LivrosSimilaresSelector(this, livroController);

        // Painel de status
        JPanel statusPanel = new JPanel(new BorderLayout(5, 0));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        // Status label
        statusLabel = new JLabel("");
        statusLabel.setForeground(Color.BLUE);

        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.add(progressBar, BorderLayout.EAST);

        // Adiciona componentes ao painel de similares
        similaresPanel.add(livrosSimilaresSelector, BorderLayout.CENTER);
        similaresPanel.add(statusPanel, BorderLayout.SOUTH);

        // Adiciona o componente ao formulário com peso para que ocupe o espaço restante
        formPanel.addFullWidthComponent(similaresPanel, FIELD_SIMILARES);

        // Adiciona o formulário ao painel principal, expandindo em todas as direções
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Define o painel principal como componente principal
        setMainComponent(mainPanel);

        // Adiciona os botões no painel inferior
        addButton(isModoEdicao ? "Atualizar" : "Salvar", e -> salvarLivro());
        addButton("Cancelar", e -> cancel());
    }

    /**
     * Configura a validação de campos obrigatórios
     */
    private void setupValidation() {
        validator = new FormValidator();

        // Adiciona campos obrigatórios
        validator.addRequiredField(formPanel.getField(FIELD_ISBN), "ISBN")
                .addRequiredField(formPanel.getField(FIELD_TITULO), "Título");
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
        // Primeiro validamos os campos
        if (!validator.validateAll()) {
            showError(validator.getErrorMessage());
            return;
        }

        String isbn = formPanel.getTextFieldValue(FIELD_ISBN).trim();
        String titulo = formPanel.getTextFieldValue(FIELD_TITULO).trim();

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
     * Busca informações do livro pelo ISBN e automaticamente busca livros relacionados
     */
    private void buscarPorIsbn() {
        // Removemos qualquer erro anterior
        validator.clearErrors();

        String isbn = formPanel.getTextFieldValue(FIELD_ISBN).trim();
        if (isbn.isEmpty()) {
            showError("Digite um ISBN para busca.");
            // Marcamos o campo de ISBN com erro
            validator.addRequiredField(formPanel.getField(FIELD_ISBN), "ISBN").validateAll();
            return;
        }

        // Desabilita o botão de busca
        buscarButton.setEnabled(false);
        buscarButton.setText("Buscando...");

        // Mostra indicador de progresso
        progressBar.setVisible(true);
        statusLabel.setText("Buscando informações do livro...");

        // Busca informações do livro e depois os relacionados
        new SwingWorker<Livro, String>() {
            @Override
            protected Livro doInBackground() {
                // Publica atualizações de status
                publish("Buscando informações do livro...");

                // Busca informações do livro pelo ISBN
                Livro resultado = livroController.buscarLivroPorIsbnApi(isbn).orElse(null);

                if (resultado != null) {
                    publish("Buscando livros relacionados...");
                }

                return resultado;
            }

            @Override
            protected void process(List<String> chunks) {
                // Atualiza o status com a mensagem mais recente
                if (!chunks.isEmpty()) {
                    statusLabel.setText(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    Livro resultado = get();
                    if (resultado != null) {
                        // Preenche os campos com os dados do livro
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

                        // Inicia a busca de livros relacionados
                        buscarLivrosRelacionados(isbn);
                    } else {
                        statusLabel.setText("Nenhum livro encontrado com este ISBN");
                        progressBar.setVisible(false);
                        buscarButton.setEnabled(true);
                        buscarButton.setText("Buscar por ISBN");
                        showInfo("Nenhum livro encontrado com ISBN: " + isbn);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    finalizarBusca("Busca interrompida");
                } catch (ExecutionException e) {
                    finalizarBusca("Erro na busca: " + e.getCause().getMessage());
                    showError("Erro na busca por ISBN: " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    /**
     * Busca livros relacionados por subjects
     */
    private void buscarLivrosRelacionados(String isbn) {
        // Status já deve estar visível da busca anterior
        statusLabel.setText("Buscando livros relacionados...");

        new SwingWorker<List<Livro>, Void>() {
            @Override
            protected List<Livro> doInBackground() {
                return livroController.buscarLivrosRelacionadosPorSubjects(isbn, 5);
            }

            @Override
            protected void done() {
                try {
                    List<Livro> livrosRelacionados = get();

                    if (livrosRelacionados.isEmpty()) {
                        finalizarBusca("Nenhum livro relacionado encontrado");
                        showInfo("Livro encontrado com sucesso, mas nenhum livro relacionado foi identificado.");
                    } else {
                        // Adiciona os livros relacionados ao componente seletor
                        int contador = 0;
                        for (Livro livroRel : livrosRelacionados) {
                            livrosSimilaresSelector.adicionarLivro(livroRel);
                            contador++;
                        }

                        finalizarBusca("Encontrados " + contador + " livros relacionados");
                        showInfo("Livro encontrado e " + contador + " livros relacionados adicionados.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    finalizarBusca("Busca interrompida");
                } catch (ExecutionException e) {
                    finalizarBusca("Erro: " + e.getCause().getMessage());
                    showError("Erro ao buscar livros relacionados: " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    /**
     * Finaliza o processo de busca resetando os controles
     */
    private void finalizarBusca(String mensagemStatus) {
        statusLabel.setText(mensagemStatus);
        progressBar.setVisible(false);
        buscarButton.setEnabled(true);
        buscarButton.setText("Buscar por ISBN");
    }

    /**
     * Retorna o livro que foi editado ou criado
     */
    public Livro getLivro() {
        return livro;
    }
}
