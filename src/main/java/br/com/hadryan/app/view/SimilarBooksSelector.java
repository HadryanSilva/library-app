package br.com.hadryan.app.view;

import br.com.hadryan.app.controller.BookController;
import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.util.MessageUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Componente para seleção de livros similares.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class SimilarBooksSelector extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextArea selectedBooksArea;
    private JButton selectButton;
    private JButton suggestButton;

    private final Set<String> selectedIsbnSet = new HashSet<>();
    private final BookController bookController;
    private final JDialog parentDialog;
    private Book currentBook;

    /**
     * Construtor do componente seletor de livros similares
     *
     * @param parentDialog Diálogo pai
     * @param bookController Controller de livros
     */
    public SimilarBooksSelector(JDialog parentDialog, BookController bookController) {
        this.parentDialog = parentDialog;
        this.bookController = bookController;

        initComponents();
        layoutComponents();
        setupListeners();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        // Área de texto para exibir ISBNs dos livros selecionados
        selectedBooksArea = new JTextArea(3, 30);
        selectedBooksArea.setLineWrap(true);
        selectedBooksArea.setWrapStyleWord(true);
        selectedBooksArea.setEditable(true);

        // Botões
        selectButton = new JButton("Selecionar Livros");
        suggestButton = new JButton("Sugerir Similares");
    }

    /**
     * Configura o layout do componente
     */
    private void layoutComponents() {
        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(suggestButton);
        buttonPanel.add(selectButton);

        // Adiciona componentes ao painel principal
        add(new JScrollPane(selectedBooksArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        selectButton.addActionListener(e -> showBookSelectorDialog());
        suggestButton.addActionListener(e -> suggestSimilarBooks());
    }

    /**
     * Exibe diálogo para seleção de livros similares
     */
    private void showBookSelectorDialog() {
        // Cria diálogo
        JDialog dialog = new JDialog(parentDialog, "Selecionar Livros Similares", true);
        dialog.setLayout(new BorderLayout());

        // Tabela de livros
        BookTablePanel bookTablePanel = new BookTablePanel();
        bookTablePanel.loadData(bookController.findAll());

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Adicionar");
        JButton closeButton = new JButton("Fechar");

        // Adiciona action listeners para os botões
        addButton.addActionListener(e -> {
            Book selectedBook = bookTablePanel.getSelectedBook();
            if (selectedBook != null) {
                // Não adiciona o livro atual como similar dele mesmo
                if (currentBook != null && currentBook.getId() != null &&
                        currentBook.getId().equals(selectedBook.getId())) {
                    MessageUtil.showWarning(dialog,
                            "Um livro não pode ser similar dele mesmo.", "Aviso");
                    return;
                }

                // Adiciona ISBN à lista se não estiver presente
                if (selectedIsbnSet.add(selectedBook.getIsbn())) {
                    updateSelectedBooksArea();
                }
            } else {
                MessageUtil.showWarning(dialog, "Selecione um livro.", "Aviso");
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(closeButton);

        // Configura e exibe o diálogo
        dialog.add(bookTablePanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(parentDialog);
        dialog.setVisible(true);
    }

    /**
     * Sugere livros similares com base no livro atual
     */
    private void suggestSimilarBooks() {
        if (currentBook == null || currentBook.getId() == null) {
            MessageUtil.showWarning(parentDialog,
                    "É necessário salvar o livro antes de sugerir similares.", "Aviso");
            return;
        }

        List<Book> suggestions = bookController.suggestSimilarBooks(currentBook, 5);

        if (suggestions.isEmpty()) {
            MessageUtil.showInfo(parentDialog,
                    "Não foram encontradas sugestões de livros similares.", "Informação");
            return;
        }

        // Adiciona as sugestões à lista de selecionados
        for (Book suggestion : suggestions) {
            selectedIsbnSet.add(suggestion.getIsbn());
        }

        updateSelectedBooksArea();

        MessageUtil.showInfo(parentDialog,
                "Foram adicionadas " + suggestions.size() + " sugestões.", "Sugestões Adicionadas");
    }

    /**
     * Atualiza a área de texto com os ISBNs selecionados
     */
    private void updateSelectedBooksArea() {
        selectedBooksArea.setText(String.join(", ", selectedIsbnSet));
    }

    /**
     * Define o livro atual
     *
     * @param book Livro atual
     */
    public void setCurrentBook(Book book) {
        this.currentBook = book;

        // Limpa e atualiza os livros similares
        selectedIsbnSet.clear();

        if (book != null && book.getSimilarBooks() != null) {
            for (Book similarBook : book.getSimilarBooks()) {
                selectedIsbnSet.add(similarBook.getIsbn());
            }
        }

        updateSelectedBooksArea();
    }

    /**
     * Obtém a lista de ISBNs dos livros similares selecionados
     *
     * @return Lista de ISBNs
     */
    public List<String> getSelectedIsbns() {
        // Se o usuário digitou manualmente, precisamos atualizar o conjunto
        parseManualInput();

        // Converte o conjunto para uma lista
        return new ArrayList<>(selectedIsbnSet);
    }

    /**
     * Obtém o texto da área de seleção
     *
     * @return Texto da área de seleção
     */
    public String getSelectedBooksText() {
        return selectedBooksArea.getText();
    }

    /**
     * Define o texto da área de seleção
     *
     * @param text Texto a ser definido
     */
    public void setSelectedBooksText(String text) {
        selectedBooksArea.setText(text);
        parseManualInput();
    }

    /**
     * Analisa a entrada manual do usuário para atualizar o conjunto de ISBNs
     */
    private void parseManualInput() {
        String text = selectedBooksArea.getText().trim();

        // Limpa o conjunto atual
        selectedIsbnSet.clear();

        // Se o texto não estiver vazio, adiciona os ISBNs ao conjunto
        if (!text.isEmpty()) {
            String[] isbnArray = text.split(",");
            for (String isbn : isbnArray) {
                String trimmedIsbn = isbn.trim();
                if (!trimmedIsbn.isEmpty()) {
                    selectedIsbnSet.add(trimmedIsbn);
                }
            }
        }
    }
}