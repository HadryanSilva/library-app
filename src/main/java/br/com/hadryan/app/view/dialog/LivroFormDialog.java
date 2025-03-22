package br.com.hadryan.app.view.dialog;

import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Editora;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.view.components.LivrosSimilaresSelector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Diálogo para criação e edição de livros.
 * Adaptado para tratar datas como String.
 */
public class LivroFormDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final Livro livro;
    private final LivroController livroController;
    private final boolean isModoEdicao;
    private boolean confirmado;

    // Componentes do formulário
    private JTextField tituloField;
    private JTextField isbnField;
    private JTextField editoraField;
    private JTextField dataPublicacaoField;
    private JTextArea autoresArea;
    private LivrosSimilaresSelector livrosSimilaresSelector;

    // Botões
    private JButton salvarButton;
    private JButton cancelarButton;
    private JButton buscarButton;

    /**
     * Construtor do diálogo
     */
    public LivroFormDialog(JFrame parent, Livro livro, LivroController livroController) {
        super(parent, (livro == null ? "Novo Livro" : "Editar Livro"), true);
        this.livro = (livro == null) ? new Livro() : livro;
        this.livroController = livroController;
        this.isModoEdicao = (livro != null && livro.getId() != null);
        this.confirmado = false;

        initComponents();
        layoutComponents();
        setupListeners();
        preencherCampos();

        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        // Campos do formulário
        tituloField = new JTextField(30);
        isbnField = new JTextField(20);
        editoraField = new JTextField(30);
        dataPublicacaoField = new JTextField(10);
        autoresArea = new JTextArea(3, 30);
        autoresArea.setLineWrap(true);
        autoresArea.setWrapStyleWord(true);

        // Componente para seleção de livros similares
        livrosSimilaresSelector = new LivrosSimilaresSelector(this, livroController);

        // Botões
        salvarButton = new JButton(isModoEdicao ? "Atualizar" : "Salvar");
        cancelarButton = new JButton("Cancelar");
        buscarButton = new JButton("Buscar por ISBN");
    }

    /**
     * Configura o layout do diálogo
     */
    private void layoutComponents() {
        // Painel do formulário
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // ISBN e botão de busca na mesma linha
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("ISBN:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(isbnField, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(buscarButton, gbc);

        // Título
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Título:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(tituloField, gbc);

        // Autores
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Autores:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JScrollPane autoresScroll = new JScrollPane(autoresArea);
        formPanel.add(autoresScroll, gbc);

        // Editora
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Editora:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(editoraField, gbc);

        // Data de publicação
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Data de Publicação:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(dataPublicacaoField, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("(Qualquer formato)"), gbc);

        // Livros similares
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(new JLabel("Livros Similares:"), gbc);

        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        formPanel.add(livrosSimilaresSelector, gbc);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(salvarButton);
        buttonPanel.add(cancelarButton);

        // Layout principal
        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Tamanho mínimo do diálogo
        setMinimumSize(new Dimension(500, 400));
        setSize(600, 500);
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        salvarButton.addActionListener(this::salvarLivro);
        cancelarButton.addActionListener(e -> dispose());
        buscarButton.addActionListener(this::buscarPorIsbn);
    }

    /**
     * Preenche os campos com os dados do livro
     */
    private void preencherCampos() {
        if (!isModoEdicao) {
            // Livro novo, deixa campos vazios
            return;
        }

        // Preenche campos com os dados do livro
        tituloField.setText(livro.getTitulo());
        isbnField.setText(livro.getIsbn());

        if (livro.getEditora() != null) {
            editoraField.setText(livro.getEditora().getNome());
        }

        if (livro.getDataPublicacao() != null) {
            dataPublicacaoField.setText(livro.getDataPublicacao());
        }

        // Autores como lista separada por vírgulas
        String autores = livro.getAutores().stream()
                .map(Autor::getNome)
                .collect(Collectors.joining(", "));
        autoresArea.setText(autores);

        // Define o livro atual no seletor de livros similares
        livrosSimilaresSelector.setLivroAtual(livro);
    }

    /**
     * Salva o livro
     */
    private void salvarLivro(ActionEvent e) {
        // Valida campos obrigatórios
        if (isbnField.getText().trim().isEmpty() || tituloField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "ISBN e Título são campos obrigatórios.",
                    "Erro de Validação",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cria ou atualiza o objeto livro
        livro.setTitulo(tituloField.getText().trim());
        livro.setIsbn(isbnField.getText().trim());

        // Data de publicação - armazena diretamente como String
        if (!dataPublicacaoField.getText().trim().isEmpty()) {
            livro.setDataPublicacao(dataPublicacaoField.getText().trim());
        }

        // Editora
        if (!editoraField.getText().trim().isEmpty()) {
            Editora editora = new Editora(editoraField.getText().trim());
            livro.setEditora(editora);
        }

        // Autores
        Set<Autor> autores = new HashSet<>();
        if (!autoresArea.getText().trim().isEmpty()) {
            String[] nomesAutores = autoresArea.getText().split(",");
            for (String nome : nomesAutores) {
                String nomeAjustado = nome.trim();
                if (!nomeAjustado.isEmpty()) {
                    autores.add(new Autor(nomeAjustado));
                }
            }
        }
        livro.setAutores(autores);

        try {
            // Salva o livro
            Livro livroSalvo = livroController.salvar(livro);

            // Atualiza os livros similares
            List<String> isbnsSimilares = livrosSimilaresSelector.getIsbnsSelcionados();
            livroController.atualizarLivrosSimilares(livroSalvo, isbnsSimilares);

            confirmado = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar livro: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Busca informações do livro pelo ISBN
     */
    private void buscarPorIsbn(ActionEvent e) {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Digite um ISBN para busca.",
                    "Erro de Validação",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Desabilita o botão de busca e mostra mensagem de progresso
        buscarButton.setEnabled(false);
        buscarButton.setText("Buscando...");

        // Utiliza SwingWorker para realizar a busca em segundo plano
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
                        // Preenche os campos com os dados recuperados
                        tituloField.setText(resultado.getTitulo());

                        if (resultado.getEditora() != null) {
                            editoraField.setText(resultado.getEditora().getNome());
                        }

                        if (resultado.getDataPublicacao() != null) {
                            dataPublicacaoField.setText(resultado.getDataPublicacao());
                        }

                        // Autores como lista separada por vírgulas
                        String autores = resultado.getAutores().stream()
                                .map(Autor::getNome)
                                .collect(Collectors.joining(", "));
                        autoresArea.setText(autores);

                        JOptionPane.showMessageDialog(LivroFormDialog.this,
                                "Livro encontrado e dados carregados.",
                                "Sucesso",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(LivroFormDialog.this,
                                "Nenhum livro encontrado com ISBN: " + isbn,
                                "Não Encontrado",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LivroFormDialog.this,
                            "Erro na busca por ISBN: " + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Reabilita o botão de busca
                    buscarButton.setEnabled(true);
                    buscarButton.setText("Buscar por ISBN");
                }
            }
        }.execute();
    }

    /**
     * Verifica se o formulário foi confirmado (salvo)
     */
    public boolean isConfirmado() {
        return confirmado;
    }

    /**
     * Retorna o livro que foi editado ou criado
     */
    public Livro getLivro() {
        return livro;
    }
}
