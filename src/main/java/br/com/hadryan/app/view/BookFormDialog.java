package br.com.hadryan.app.view;

import br.com.hadryan.app.controller.BookController;
import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.model.entity.Publisher;
import br.com.hadryan.app.service.OpenLibraryService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BookFormDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final Book book;
    private final BookController bookController;
    private final OpenLibraryService openLibraryService;
    private final boolean isEditMode;
    private boolean confirmed;

    // Form fields
    private JTextField titleField;
    private JTextField isbnField;
    private JTextField publisherField;
    private JTextField pubDateField;
    private JTextArea authorsArea;
    private JTextArea similarBooksArea;

    // Buttons
    private JButton saveButton;
    private JButton cancelButton;
    private JButton lookupButton;

    /**
     * Constructs the book form dialog
     *
     * @param parent Parent frame
     * @param book Book to edit, or null for a new book
     */
    public BookFormDialog(JFrame parent, Book book) {
        super(parent, "Book Form", true);
        this.book = book;
        this.bookController = new BookController();
        this.openLibraryService = new OpenLibraryService();
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
     * Initializes the UI components
     */
    private void initComponents() {
        // Form fields
        titleField = new JTextField(30);
        isbnField = new JTextField(20);
        publisherField = new JTextField(30);
        pubDateField = new JTextField(10);
        authorsArea = new JTextArea(3, 30);
        similarBooksArea = new JTextArea(3, 30);

        // Buttons
        saveButton = new JButton(isEditMode ? "Update" : "Save");
        cancelButton = new JButton("Cancel");
        lookupButton = new JButton("Lookup ISBN");
    }

    /**
     * Lays out the components
     */
    private void layoutComponents() {
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // ISBN and lookup button in same row
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("ISBN:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(isbnField, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(lookupButton, gbc);

        // Title
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(titleField, gbc);

        // Authors
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Authors:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane authorsScroll = new JScrollPane(authorsArea);
        formPanel.add(authorsScroll, gbc);

        // Publisher
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Publisher:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(publisherField, gbc);

        // Publication Date
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Publication Date:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(pubDateField, gbc);

        // Similar Books
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Similar Books:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane similarBooksScroll = new JScrollPane(similarBooksArea);
        formPanel.add(similarBooksScroll, gbc);

        // Help text for similar books
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        formPanel.add(new JLabel("(Enter ISBNs separated by commas)"), gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Main layout
        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        saveButton.addActionListener(this::saveBook);
        cancelButton.addActionListener(e -> dispose());
        lookupButton.addActionListener(this::lookupByIsbn);
    }

    /**
     * Populates the form fields with book data
     */
    private void populateFields() {
        if (!isEditMode) {
            // New book, leave fields empty
            return;
        }

        // Populate fields with book data
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

        // Similar books as comma-separated ISBNs
        String similarBooks = book.getSimilarBooks().stream()
                .map(Book::getIsbn)
                .collect(Collectors.joining(", "));
        similarBooksArea.setText(similarBooks);
    }

    /**
     * Saves the book data
     *
     * @param e Action event
     */
    private void saveBook(ActionEvent e) {
        // Validate required fields
        if (isbnField.getText().trim().isEmpty() || titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "ISBN and Title are required fields.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create or update book object
        Book bookToSave = isEditMode ? book : new Book();

        // Set basic properties
        bookToSave.setTitle(titleField.getText().trim());
        bookToSave.setIsbn(isbnField.getText().trim());

        // Parse publication date
        if (!pubDateField.getText().trim().isEmpty()) {
            LocalDate pubDate = LocalDate.parse(pubDateField.getText().trim());
            bookToSave.setPublicationDate(pubDate);
        }

        // Set publisher
        if (!publisherField.getText().trim().isEmpty()) {
            Publisher publisher = new Publisher(publisherField.getText().trim());
            bookToSave.setPublisher(publisher);
        }

        // Set authors
        Set<Author> authors = new HashSet<>();
        if (!authorsArea.getText().trim().isEmpty()) {
            String[] authorNames = authorsArea.getText().split(",");
            for (String name : authorNames) {
                if (!name.trim().isEmpty()) {
                    authors.add(new Author(name.trim()));
                }
            }
        }
        bookToSave.setAuthors(authors);

        // Set similar books
        if (!similarBooksArea.getText().trim().isEmpty()) {
            Set<Book> similarBooks = new HashSet<>();
            String[] isbnList = similarBooksArea.getText().split(",");
            for (String isbn : isbnList) {
                isbn = isbn.trim();
                if (!isbn.isEmpty()) {
                    Optional<Book> similarBook = bookController.findByIsbn(isbn);
                    similarBook.ifPresent(similarBooks::add);
                }
            }
            bookToSave.setSimilarBooks(similarBooks);
        }

        try {
            // Save the book
            bookController.save(bookToSave);
            confirmed = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving book: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Looks up book information by ISBN
     *
     * @param e Action event
     */
    private void lookupByIsbn(ActionEvent e) {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter an ISBN to lookup.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable the lookup button and show a progress message
        lookupButton.setEnabled(false);
        lookupButton.setText("Looking up...");

        // Use SwingWorker to perform the lookup in the background
        new SwingWorker<Optional<Book>, Void>() {
            @Override
            protected Optional<Book> doInBackground() {
                return openLibraryService.findBookByIsbn(isbn);
            }

            @Override
            protected void done() {
                try {
                    Optional<Book> result = get();
                    if (result.isPresent()) {
                        // Populate the form fields with the retrieved data
                        Book lookupBook = result.get();
                        titleField.setText(lookupBook.getTitle());

                        if (lookupBook.getPublisher() != null) {
                            publisherField.setText(lookupBook.getPublisher().getName());
                        }

                        if (lookupBook.getPublicationDate() != null) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            pubDateField.setText(dateFormat.format(lookupBook.getPublicationDate()));
                        }

                        // Authors as comma-separated list
                        String authors = lookupBook.getAuthors().stream()
                                .map(Author::getName)
                                .collect(Collectors.joining(", "));
                        authorsArea.setText(authors);

                        JOptionPane.showMessageDialog(BookFormDialog.this,
                                "Book found and data loaded.",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(BookFormDialog.this,
                                "No book found with ISBN: " + isbn,
                                "Not Found", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BookFormDialog.this,
                            "Error looking up ISBN: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Re-enable the lookup button
                    lookupButton.setEnabled(true);
                    lookupButton.setText("Lookup ISBN");
                }
            }
        }.execute();
    }

    /**
     * Checks if the form was confirmed (saved)
     *
     * @return true if saved, false otherwise
     */
    public boolean isConfirmed() {
        return confirmed;
    }

}
