package br.com.hadryan.app.view;

import br.com.hadryan.app.util.JPAUtil;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Panel references
    private BookListPanel bookListPanel;
    private BookSearchPanel bookSearchPanel;
    private ImportPanel importPanel;

    /**
     * Constructs the main application frame
     */
    public MainFrame() {
        initComponents();
        setupLayout();
        setupMenu();
        setupWindowListeners();
    }

    /**
     * Initializes the UI components
     */
    private void initComponents() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null);

        // Initialize card layout for content switching
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Initialize panels
        bookListPanel = new BookListPanel(this);
        bookSearchPanel = new BookSearchPanel(this);
        importPanel = new ImportPanel(this);
    }

    /**
     * Sets up the layout of the frame
     */
    private void setupLayout() {
        // Add panels to card layout
        contentPanel.add(bookListPanel, "LIST");
        contentPanel.add(bookSearchPanel, "SEARCH");
        contentPanel.add(importPanel, "IMPORT");

        // Add content panel to frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        // Show the book list panel by default
        cardLayout.show(contentPanel, "LIST");
    }

    /**
     * Sets up the menu bar
     */
    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(exitItem);

        // Books Menu
        JMenu booksMenu = new JMenu("Books");

        JMenuItem listItem = new JMenuItem("Book List");
        listItem.addActionListener(e -> showPanel("LIST"));

        JMenuItem searchItem = new JMenuItem("Search Books");
        searchItem.addActionListener(e -> showPanel("SEARCH"));

        JMenuItem importItem = new JMenuItem("Import Books");
        importItem.addActionListener(e -> showPanel("IMPORT"));

        booksMenu.add(listItem);
        booksMenu.add(searchItem);
        booksMenu.addSeparator();
        booksMenu.add(importItem);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());

        helpMenu.add(aboutItem);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(booksMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Sets up window listeners
     */
    private void setupWindowListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }

    /**
     * Shows a specific panel in the card layout
     *
     * @param panelName Name of the panel to show
     */
    public void showPanel(String panelName) {
        if (panelName.equals("LIST")) {
            bookListPanel.refreshData();
        }
        cardLayout.show(contentPanel, panelName);
    }

    /**
     * Shows the about dialog
     */
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Library Management System\nVersion 1.0\n\nDeveloped by Your Name",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exits the application, closing resources
     */
    private void exitApplication() {
        // Close resources
        JPAUtil.close();

        // Exit application
        System.exit(0);
    }

    /**
     * Application entry point
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Create and show main frame
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

}
