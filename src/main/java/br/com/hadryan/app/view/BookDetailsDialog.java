package br.com.hadryan.app.view;

import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class BookDetailsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final Book book;

    // Display fields
    private JTextField titleField;
    private JTextField isbnField;
    private JTextField publisherField;
    private JTextField pubDateField;
    private JTextArea authorsArea;
    private JTextArea similarBooksArea;

    /**
     * Constructs the book details dialog
     *
     * @param parent Parent frame
     * @param book Book to display
     */
    public BookDetailsDialog(JFrame parent, Book book) {
        super(parent, "Book Details", true);
        this.book = book;

        initComponents();
        layoutComponents();
        populateFields();

        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Initializes the UI components
     */
    private void initComponents() {
        // Display fields - all read-only
        titleField = new JTextField(30);
        titleField.setEditable(false);

        isbnField = new JTextField(20);
        isbnField.setEditable(false);

        publisherField = new JTextField(30);
        publisherField.setEditable(false);

        pubDateField = new JTextField(10);
        pubDateField.setEditable(false);

        authorsArea = new JTextArea(3, 30);
        authorsArea.setEditable(false);
        authorsArea.setLineWrap(true);
        authorsArea.setWrapStyleWord(true);

        similarBooksArea = new JTextArea(3, 30);
        similarBooksArea.setEditable(false);
        similarBooksArea.setLineWrap(true);
        similarBooksArea.setWrapStyleWord(true);
    }

    /**
     * Lays out the components
     */
    private void layoutComponents() {
        // Content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(titleField, gbc);

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

        // Authors
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Authors:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JScrollPane authorsScroll = new JScrollPane(authorsArea);
        contentPanel.add(authorsScroll, gbc);

        // Publisher
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Publisher:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(publisherField, gbc);

        // Publication Date
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Publication Date:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(pubDateField, gbc);

        // Similar Books
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Similar Books:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JScrollPane similarBooksScroll = new JScrollPane(similarBooksArea);
        contentPanel.add(similarBooksScroll, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        // Main layout
        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Populates the fields with book data
     */
    private void populateFields() {
        // Populate basic fields
        titleField.setText(book.getTitle());
        isbnField.setText(book.getIsbn());

        if (book.getPublisher() != null) {
            publisherField.setText(book.getPublisher().getName());
        }

        if (book.getPublicationDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            pubDateField.setText(dateFormat.format(book.getPublicationDate()));
        }

        // Authors as comma-separated list
        String authors = book.getAuthors().stream()
                .map(Author::getName)
                .collect(Collectors.joining(", "));
        authorsArea.setText(authors);

        // Similar books as formatted list
        String similarBooks = book.getSimilarBooks().stream()
                .map(b -> b.getTitle() + " (ISBN: " + b.getIsbn() + ")")
                .collect(Collectors.joining("\n"));
        similarBooksArea.setText(similarBooks);
    }

}
