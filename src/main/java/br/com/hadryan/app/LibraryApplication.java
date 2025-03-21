package br.com.hadryan.app;

import br.com.hadryan.app.model.repository.RepositoryFactory;
import br.com.hadryan.app.service.ServiceFactory;
import br.com.hadryan.app.util.JPAUtil;
import br.com.hadryan.app.util.MessageUtil;
import br.com.hadryan.app.util.UIUtil;
import br.com.hadryan.app.view.MainFrame;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principal da aplicação.
 * Responsável por inicializar os recursos e abrir a janela principal.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class LibraryApplication {

    private static final Logger LOGGER = Logger.getLogger(LibraryApplication.class.getName());

    /**
     * Ponto de entrada da aplicação
     *
     * @param args Argumentos de linha de comando
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Configura a aparência da aplicação
                UIUtil.setupLookAndFeel();

                // Inicializa o banco de dados
                initializeDatabase();

                // Cria e exibe a janela principal
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao iniciar a aplicação", e);

                MessageUtil.showError(null,
                        "Erro ao iniciar a aplicação: " + e.getMessage(),
                        "Erro de Inicialização");

                // Fecha recursos
                cleanupResources();

                System.exit(1);
            }
        });
    }

    /**
     * Inicializa a conexão com o banco de dados
     *
     * @throws Exception se a inicialização do banco de dados falhar
     */
    private static void initializeDatabase() throws Exception {
        try {
            // Testa a conexão com o banco de dados
            JPAUtil.getEntityManager().close();
            LOGGER.info("Conexão com o banco de dados estabelecida com sucesso.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Falha ao inicializar o banco de dados", e);
            throw new Exception("Falha ao inicializar o banco de dados: " + e.getMessage(), e);
        }
    }

    /**
     * Limpa recursos utilizados pela aplicação
     */
    public static void cleanupResources() {
        try {
            // Fecha as factories
            ServiceFactory.getInstance().closeAll();
            RepositoryFactory.getInstance().closeAll();

            // Fecha o EntityManagerFactory
            JPAUtil.close();

            LOGGER.info("Recursos liberados com sucesso.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao liberar recursos", e);
        }
    }
}
