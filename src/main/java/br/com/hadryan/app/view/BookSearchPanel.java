package br.com.hadryan.app.view;

import br.com.hadryan.app.controller.BookController;
import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.model.entity.Publisher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import java.util.List;
import java.util.stream.Collectors;

public class BookSearchPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame parentFrame;
    private final BookController bookController;

    // Search fields
    private JTextField titleField;
    private JTextField isbnField;
    private JTextField authorField;
    private JTextField publisherField;
    private JTextField pubDateField;

    // Result table
    private JTable resultTable;
    private DefaultTableModel tableModel;

    // Buttons
    private JButton searchButton;
    private JButton clearButton;
    private JButton viewButton;
    private JButton backButton;

    /**
     * Constructs the book search panel
     *
     * @param parentFrame Parent frame
     */
    public BookSearchPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.bookController = new BookController();

        initComponents();
        layoutComponents();
        setupListeners();
    }

    /**
     * Initializes the UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        // Search fields
        titleField = new JTextField(20);
        isbnField = new JTextField(15);
        authorField = new JTextField(20);
        publisherField = new JTextField(20);
        pubDateField = new JTextField(10);

        // Table model with non-editable cells
        tableModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Setup table columns
        tableModel.addColumn("ID");
        tableModel.addColumn("Title");
        tableModel.addColumn("Authors");
        tableModel.addColumn("ISBN");
        tableModel.addColumn("Publisher");
        tableModel.addColumn("Publication Date");

        // Create table
        resultTable = new JTable(tableModel);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.getTableHeader().setReorderingAllowed(false);

        // Create buttons
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");
        viewButton = new JButton("View Details");
        backButton = new JButton("Back to List");

        // Initially disable view button
        viewButton.setEnabled(false);
    }

    /**
     * Lays out the components
     */
    private void layoutComponents() {
        // Search panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(titleField, gbc);

        // ISBN
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("ISBN:"), gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(isbnField, gbc);

        // Author
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("Author:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(authorField, gbc);

        // Publisher
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("Publisher:"), gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(publisherField, gbc);

        // Publication Date
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("Publication Date:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(pubDateField, gbc);

        // Date format hint
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("(yyyy-MM-dd)"), gbc);

        // Search buttons
        JPanel searchButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButtonPanel.add(searchButton);
        searchButtonPanel.add(clearButton);

        // Top panel containing search form and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(searchButtonPanel, BorderLayout.SOUTH);

        // Result panel buttons
        JPanel resultButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        resultButtonPanel.add(viewButton);
        resultButtonPanel.add(backButton);

        // Main layout
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);
        add(resultButtonPanel, BorderLayout.SOUTH);
    }

    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        // Table selection listener
        resultTable.getSelectionModel().addListSelectionListener(e -> {
            viewButton.setEnabled(resultTable.getSelectedRow() != -1);
        });

        // Button actions
        searchButton.addActionListener(e -> performSearch());
        clearButton.addActionListener(e -> clearForm());
        viewButton.addActionListener(e -> viewSelectedBook());
        backButton.addActionListener(e -> parentFrame.showPanel("LIST"));
    }

    /**
     * Performs the search based on the criteria
     */
    private void performSearch() {
        // Create search criteria
        Book searchCriteria = new Book();

        // Set title if provided
        if (!titleField.getText().trim().isEmpty()) {
            searchCriteria.setTitle(titleField.getText().trim());
        }

        // Set ISBN if provided
        if (!isbnField.getText().trim().isEmpty()) {
            searchCriteria.setIsbn(isbnField.getText().trim());
        }

        // Set author if provided
        if (!authorField.getText().trim().isEmpty()) {
            Author author = new Author(authorField.getText().trim());
            searchCriteria.addAuthor(author);
        }

        // Set publisher if provided
        if (!publisherField.getText().trim().isEmpty()) {
            Publisher publisher = new Publisher(publisherField.getText().trim());
            searchCriteria.setPublisher(publisher);
        }

        // Set publication date if provided
        if (!pubDateField.getText().trim().isEmpty()) {
            searchCriteria.setPublicationDate(LocalDate.parse(pubDateField.getText().trim()));
        }

        // Perform search
        List<Book> results = bookController.search(searchCriteria);
        displayResults(results);
    }

    /**
     * Displays the search results in the table
     *
     * @param books List of books to display
     */
    private void displayResults(List<Book> books) {
        // Clear existing data
        tableModel.setRowCount(0);

        // Add books to table
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (Book book : books) {
            // Format authors as comma-separated string
            String authors = book.getAuthors().stream()
                    .map(Author::getName)
                    .collect(Collectors.joining(", "));

            // Format publication date
            String pubDate = book.getPublicationDate() != null
                    ? dateFormat.format(book.getPublicationDate())
                    : "";

            // Add row to table
            tableModel.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    authors,
                    book.getIsbn(),
                    book.getPublisher() != null ? book.getPublisher().getName() : "",
                    pubDate
            });
        }

        // Show result count
        JOptionPane.showMessageDialog(this,
                books.size() + " book(s) found.",
                "Search Results", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Clears the search form
     */
    private void clearForm() {
        titleField.setText("");
        isbnField.setText("");
        authorField.setText("");
        publisherField.setText("");
        pubDateField.setText("");
        tableModel.setRowCount(0);
    }

    /**
     * Views the selected book details
     */
    private void viewSelectedBook() {
        int selectedRow = resultTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        // Get book ID from selected row
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);

        // Find the book
        bookController.findById(id).ifPresent(book -> {
            // Open book form dialog in view-only mode
            BookDetailsDialog dialog = new BookDetailsDialog(parentFrame, book);
            dialog.setVisible(true);
        });
    }

}
