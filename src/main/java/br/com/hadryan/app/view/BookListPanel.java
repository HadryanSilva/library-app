package br.com.hadryan.app.view;

import br.com.hadryan.app.controller.BookController;
import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.List;

public class BookListPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame parentFrame;
    private final BookController bookController;

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;

    /**
     * Constructs the book list panel
     *
     * @param parentFrame Parent frame
     */
    public BookListPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.bookController = new BookController();

        initComponents();
        layoutComponents();
        setupListeners();
        loadData();
    }

    /**
     * Initializes the UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout());

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
        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getTableHeader().setReorderingAllowed(false);

        // Create buttons
        addButton = new JButton("Add Book");
        editButton = new JButton("Edit Book");
        deleteButton = new JButton("Delete Book");
        refreshButton = new JButton("Refresh");

        // Initially disable edit/delete buttons
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    /**
     * Lays out the components
     */
    private void layoutComponents() {
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        // Add components to main panel
        add(new JScrollPane(bookTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        // Table selection listener
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = bookTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });

        // Double-click to edit
        bookTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1) {
                    editSelectedBook();
                }
            }
        });

        // Button actions
        addButton.addActionListener(this::addBook);
        editButton.addActionListener(e -> editSelectedBook());
        deleteButton.addActionListener(this::deleteSelectedBook);
        refreshButton.addActionListener(e -> refreshData());
    }

    /**
     * Loads data into the table
     */
    private void loadData() {
        // Clear existing data
        tableModel.setRowCount(0);

        // Get all books
        List<Book> books = bookController.findAll();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Add books to table
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
    }

    /**
     * Refreshes the data in the table
     */
    public void refreshData() {
        loadData();
    }

    /**
     * Handles the Add Book action
     *
     * @param e Action event
     */
    private void addBook(ActionEvent e) {
        // Open book form dialog in add mode
        BookFormDialog dialog = new BookFormDialog(parentFrame, null);
        dialog.setVisible(true);

        // Refresh data if dialog was not canceled
        if (dialog.isConfirmed()) {
            refreshData();
        }
    }

    /**
     * Edits the selected book
     */
    private void editSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        // Get book ID from selected row
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);

        // Find the book
        Optional<Book> bookOpt = bookController.findById(id);
        if (!bookOpt.isPresent()) {
            JOptionPane.showMessageDialog(this,
                    "Book not found. It may have been deleted.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Open book form dialog in edit mode
        BookFormDialog dialog = new BookFormDialog(parentFrame, bookOpt.get());
        dialog.setVisible(true);

        // Refresh data if dialog was not canceled
        if (dialog.isConfirmed()) {
            refreshData();
        }
    }

    /**
     * Handles the Delete Book action
     *
     * @param e Action event
     */
    private void deleteSelectedBook(ActionEvent e) {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        // Get book ID and title from selected row
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String title = (String) tableModel.getValueAt(selectedRow, 1);

        // Confirm deletion
        int response = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the book \"" + title + "\"?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            try {
                // Delete the book
                bookController.delete(id);
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "Book deleted successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting book: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
