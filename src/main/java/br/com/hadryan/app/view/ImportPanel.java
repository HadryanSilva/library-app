package br.com.hadryan.app.view;

import br.com.hadryan.app.service.ImportService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ImportPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame parentFrame;
    private final ImportService importService;

    // UI components
    private JTextField filePathField;
    private JButton browseButton;
    private JButton importButton;
    private JButton backButton;
    private JTextArea logArea;

    /**
     * Constructs the import panel
     *
     * @param parentFrame Parent frame
     */
    public ImportPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.importService = new ImportService();

        initComponents();
        layoutComponents();
        setupListeners();
    }

    /**
     * Initializes the UI components
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        // File selection
        filePathField = new JTextField(30);
        filePathField.setEditable(false);

        browseButton = new JButton("Browse...");
        importButton = new JButton("Import");
        backButton = new JButton("Back to List");

        // Log area
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);

        // Initially disable import button
        importButton.setEnabled(false);
    }

    /**
     * Lays out the components
     */
    private void layoutComponents() {
        // File selection panel
        JPanel filePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        filePanel.add(new JLabel("Import File:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        filePanel.add(filePathField, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        filePanel.add(browseButton, gbc);

        // Import info panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(new JLabel("Supported file formats:"), gbc);

        gbc.gridy = 1;
        infoPanel.add(new JLabel("- CSV (comma-separated values)"), gbc);

        gbc.gridy = 2;
        infoPanel.add(new JLabel("- XML (eXtensible Markup Language)"), gbc);

        gbc.gridy = 3;
        infoPanel.add(new JLabel("Note: During import, existing books will be updated."), gbc);

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filePanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(importButton);
        buttonPanel.add(backButton);

        // Main layout
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        browseButton.addActionListener(e -> browsefile());
        importButton.addActionListener(e -> importFile());
        backButton.addActionListener(e -> parentFrame.showPanel("LIST"));
    }

    /**
     * Opens a file chooser to select an import file
     */
    private void browsefile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Import File");

        // Set up file filters
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter(
                "CSV Files (*.csv)", "csv");
        FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter(
                "XML Files (*.xml)", "xml");

        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.addChoosableFileFilter(xmlFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(csvFilter);

        // Show open dialog
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            importButton.setEnabled(true);
            logArea.setText(""); // Clear log area
        }
    }

    /**
     * Imports books from the selected file
     */
    private void importFile() {
        String filePath = filePathField.getText();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a file to import.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File importFile = new File(filePath);
        if (!importFile.exists() || !importFile.isFile()) {
            JOptionPane.showMessageDialog(this,
                    "The selected file does not exist.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable import button during import
        importButton.setEnabled(false);
        logArea.setText("Starting import...\n");

        // Use SwingWorker to perform import in background
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return performImport(importFile);
            }

            @Override
            protected void done() {
                try {
                    int count = get();
                    logArea.append("Import completed. " + count + " books imported or updated.\n");
                    JOptionPane.showMessageDialog(ImportPanel.this,
                            count + " books imported or updated successfully.",
                            "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    logArea.append("Error during import: " + e.getMessage() + "\n");
                    JOptionPane.showMessageDialog(ImportPanel.this,
                            "Error during import: " + e.getMessage(),
                            "Import Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    importButton.setEnabled(true);
                }
            }
        }.execute();
    }

    /**
     * Performs the actual import process
     *
     * @param file File to import
     * @return Number of books imported
     * @throws IOException if an I/O error occurs
     */
    private int performImport(File file) throws IOException {
        logArea.append("Importing file: " + file.getName() + "\n");

        // Get file extension
        String fileName = file.getName().toLowerCase();
        String fileType = fileName.endsWith(".csv") ? "CSV" :
                fileName.endsWith(".xml") ? "XML" : "Unknown";

        logArea.append("Detected file type: " + fileType + "\n");
        logArea.append("Processing...\n");

        // Perform import
        int count = importService.importBooks(file);

        return count;
    }

}
