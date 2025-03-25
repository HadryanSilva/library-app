package br.com.hadryan.app;

import br.com.hadryan.app.config.JpaConfig;
import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.model.repository.LivroRepository;
import br.com.hadryan.app.service.LivroService;
import br.com.hadryan.app.service.OpenLibraryService;
import br.com.hadryan.app.service.importacao.ImportService;
import br.com.hadryan.app.view.MainFrame;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principal da aplicação.
 * Responsável pela inicialização dos componentes e lançamento da interface gráfica.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class LibraryApplication {

    private static final Logger LOGGER = Logger.getLogger(LibraryApplication.class.getName());

    /**
     * Método principal - ponto de entrada da aplicação
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                inicializarBancoDados();
                LivroRepository livroRepository = new LivroRepository();
                OpenLibraryService openLibraryService = new OpenLibraryService();

                LivroService livroService = new LivroService(livroRepository, openLibraryService);
                ImportService importService = new ImportService(livroRepository);

                LivroController livroController = new LivroController(livroService);

                MainFrame mainFrame = new MainFrame(livroController, importService);
                mainFrame.setVisible(true);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao iniciar a aplicação", e);
                JOptionPane.showMessageDialog(null,
                        "Erro ao iniciar a aplicação: " + e.getMessage(),
                        "Erro de Inicialização",
                        JOptionPane.ERROR_MESSAGE);
                liberarRecursos();
                System.exit(1);
            }
        });
    }

    /**
     * Inicializa o banco de dados e verifica a conexão
     */
    private static void inicializarBancoDados() throws Exception {
        JpaConfig jpaConfig = JpaConfig.getInstance();

        if (!jpaConfig.testarConexao()) {
            throw new Exception("Não foi possível conectar ao banco de dados.");
        }

        LOGGER.info("Conexão com o banco de dados estabelecida com sucesso.");
    }

    /**
     * Libera recursos utilizados pela aplicação
     */
    public static void liberarRecursos() {
        try {
            JpaConfig.getInstance().close();
            LOGGER.info("Recursos liberados com sucesso.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao liberar recursos", e);
        }
    }
}
