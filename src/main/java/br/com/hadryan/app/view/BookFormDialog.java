package br.com.hadryan.app.view;

import br.com.hadryan.app.controller.BookController;
import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.model.entity.Publisher;
import br.com.hadryan.app.util.DateUtil;
import br.com.hadryan.app.util.MessageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Diálogo para criação e edição de livros.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class BookFormDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final Book book;
    private final BookController bookController;
    private final boolean isEditMode;
    private boolean confirmed;

    // Componentes do formulário
    private JTextField titleField;
    private JTextField isbnField;
    private JTextField publisherField;
    private JTextField pubDateField;
    private JTextArea authorsArea;
    private SimilarBooksSelector similarBooksSelector;

    // Botões
    private JButton saveButton;
    private JButton cancelButton;
    private JButton lookupButton;

    /**
     * Construtor do diálogo
     *
     * @param parent Frame pai
     * @param book Livro a ser editado, ou null para um novo livro
     */
    public BookFormDialog(JFrame parent, Book book) {
        super(parent, (book == null ? "Novo Livro" : "Editar Livro"), true);
        this.book = (book == null) ? new Book() : book;
        this.bookController = new BookController();
        this.isEditMode = (book != null);
        this.confirmed = false;

        initComponents();
        layoutComponents();
        setupListeners();
        populateFields();

        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        // Campos do formulário
        titleField = new JTextField(30);
        isbnField = new JTextField(20);
        publisherField = new JTextField(30);
        pubDateField = new JTextField(10);
        authorsArea = new JTextArea(3, 30);
        authorsArea.setLineWrap(true);
        authorsArea.setWrapStyleWord(true);

        // Componente para seleção de livros similares
        similarBooksSelector = new SimilarBooksSelector(this, bookController);

        // Botões
        saveButton = new JButton(isEditMode ? "Atualizar" : "Salvar");
        cancelButton = new JButton("Cancelar");
        lookupButton = new JButton("Buscar por ISBN");
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
        formPanel.add(lookupButton, gbc);

        // Título
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Título:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(titleField, gbc);

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
        JScrollPane authorsScroll = new JScrollPane(authorsArea);
        formPanel.add(authorsScroll, gbc);

        // Publisher
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
        formPanel.add(publisherField, gbc);

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
        formPanel.add(pubDateField, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("(yyyy-MM-dd)"), gbc);

        // Livros similares
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(new JLabel("Livros Similares (ISBNs separados por vírgula):"), gbc);

        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        formPanel.add(similarBooksSelector, gbc);

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

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
        saveButton.addActionListener(this::saveBook);
        cancelButton.addActionListener(e -> dispose());
        lookupButton.addActionListener(this::lookupByIsbn);
    }

    /**
     * Preenche os campos com os dados do livro
     */
    private void populateFields() {
        if (!isEditMode) {
            // Livro novo, deixa campos vazios
            return;
        }

        // Preenche campos com os dados do livro
        titleField.setText(book.getTitle());
        isbnField.setText(book.getIsbn());

        if (book.getPublisher() != null) {
            publisherField.setText(book.getPublisher().getName());
        }

        if (book.getPublicationDate() != null) {
            pubDateField.setText(DateUtil.format(book.getPublicationDate()));
        }

        // Autores como lista separada por vírgulas
        String authors = book.getAuthors().stream()
                .map(Author::getName)
                .collect(Collectors.joining(", "));
        authorsArea.setText(authors);

        // Define o livro atual no seletor de livros similares
        similarBooksSelector.setCurrentBook(book);
    }

    /**
     * Salva o livro
     *
     * @param e Evento de ação
     */
    private void saveBook(ActionEvent e) {
        // Valida campos obrigatórios
        if (isbnField.getText().trim().isEmpty() || titleField.getText().trim().isEmpty()) {
            MessageUtil.showError(this,
                    "ISBN e Título são campos obrigatórios.",
                    "Erro de Validação");
            return;
        }

        // Cria ou atualiza o objeto livro
        book.setTitle(titleField.getText().trim());
        book.setIsbn(isbnField.getText().trim());

        // Data de publicação
        if (!pubDateField.getText().trim().isEmpty()) {
            LocalDate pubDate = DateUtil.parse(pubDateField.getText().trim());
            if (pubDate == null) {
                MessageUtil.showError(this,
                        "Formato de data inválido. Use o formato yyyy-MM-dd.",
                        "Erro de Validação");
                return;
            }
            book.setPublicationDate(pubDate);
        }

        // Editora
        if (!publisherField.getText().trim().isEmpty()) {
            Publisher publisher = new Publisher(publisherField.getText().trim());
            book.setPublisher(publisher);
        }

        // Autores
        Set<Author> authors = new HashSet<>();
        if (!authorsArea.getText().trim().isEmpty()) {
            String[] authorNames = authorsArea.getText().split(",");
            for (String name : authorNames) {
                String trimmedName = name.trim();
                if (!trimmedName.isEmpty()) {
                    authors.add(new Author(trimmedName));
                }
            }
        }
        book.setAuthors(authors);

        try {
            // Salva o livro
            Book savedBook = bookController.save(book);

            // Atualiza os livros similares
            List<String> similarIsbns = similarBooksSelector.getSelectedIsbns();
            bookController.updateSimilarBooks(savedBook, similarIsbns);

            confirmed = true;
            dispose();
        } catch (Exception ex) {
            MessageUtil.showError(this,
                    "Erro ao salvar livro: " + ex.getMessage(),
                    "Erro");
        }
    }

    /**
     * Busca informações do livro pelo ISBN
     *
     * @param e Evento de ação
     */
    private void lookupByIsbn(ActionEvent e) {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) {
            MessageUtil.showError(this,
                    "Digite um ISBN para busca.",
                    "Erro de Validação");
            return;
        }

        // Desabilita o botão de busca e mostra mensagem de progresso
        lookupButton.setEnabled(false);
        lookupButton.setText("Buscando...");

        // Utiliza SwingWorker para realizar a busca em segundo plano
        new SwingWorker<Book, Void>() {
            @Override
            protected Book doInBackground() {
                return bookController.findBookByIsbnAPI(isbn)
                        .orElse(null);
            }

            @Override
            protected void done() {
                try {
                    Book result = get();
                    if (result != null) {
                        // Preenche os campos com os dados recuperados
                        titleField.setText(result.getTitle());

                        if (result.getPublisher() != null) {
                            publisherField.setText(result.getPublisher().getName());
                        }

                        if (result.getPublicationDate() != null) {
                            pubDateField.setText(DateUtil.format(result.getPublicationDate()));
                        }

                        // Autores como lista separada por vírgulas
                        String authors = result.getAuthors().stream()
                                .map(Author::getName)
                                .collect(Collectors.joining(", "));
                        authorsArea.setText(authors);

                        MessageUtil.showInfo(BookFormDialog.this,
                                "Livro encontrado e dados carregados.",
                                "Sucesso");
                    } else {
                        MessageUtil.showWarning(BookFormDialog.this,
                                "Nenhum livro encontrado com ISBN: " + isbn,
                                "Não Encontrado");
                    }
                } catch (Exception ex) {
                    MessageUtil.showError(BookFormDialog.this,
                            "Erro na busca por ISBN: " + ex.getMessage(),
                            "Erro");
                } finally {
                    // Reabilita o botão de busca
                    lookupButton.setEnabled(true);
                    lookupButton.setText("Buscar por ISBN");
                }
            }
        }.execute();
    }

    /**
     * Verifica se o formulário foi confirmado (salvo)
     *
     * @return true se foi salvo, false caso contrário
     */
    public boolean isConfirmed() {
        return confirmed;
    }
}
