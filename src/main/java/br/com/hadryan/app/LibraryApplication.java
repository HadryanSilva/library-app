package br.com.hadryan.app;

import br.com.hadryan.app.util.JPAUtil;
import br.com.hadryan.app.view.MainFrame;

import javax.swing.*;

public class LibraryApplication {

    /**
     * Application entry point
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set look and feel to system default
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Initialize database connection if needed
                initializeDatabase();

                // Create and show main application frame
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Application Error", JOptionPane.ERROR_MESSAGE);

                // Close resources
                JPAUtil.close();

                System.exit(1);
            }
        });
    }

    /**
     * Initializes the database connection
     *
     * @throws Exception if database initialization fails
     */
    private static void initializeDatabase() throws Exception {
        try {
            // Test database connection by getting an EntityManager
            JPAUtil.getEntityManager().close();
        } catch (Exception e) {
            throw new Exception("Failed to initialize database: " + e.getMessage(), e);
        }
    }

}
