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
 */
public class LibraryApplication {

    private static final Logger LOGGER = Logger.getLogger(LibraryApplication.class.getName());

    /**
     * Método principal - ponto de entrada da aplicação
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Configura o Look and Feel para uma aparência nativa
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Inicializa o banco de dados
                inicializarBancoDados();

                // Inicializa os serviços e repositórios
                LivroRepository livroRepository = new LivroRepository();
                OpenLibraryService openLibraryService = new OpenLibraryService();

                // Cria e configura serviços
                LivroService livroService = new LivroService(livroRepository, openLibraryService);
                ImportService importService = new ImportService(livroRepository);

                // Cria o controller
                LivroController livroController = new LivroController(livroService);

                // Cria e exibe a interface gráfica
                MainFrame mainFrame = new MainFrame(livroController, importService);
                mainFrame.setVisible(true);

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao iniciar a aplicação", e);
                JOptionPane.showMessageDialog(null,
                        "Erro ao iniciar a aplicação: " + e.getMessage(),
                        "Erro de Inicialização",
                        JOptionPane.ERROR_MESSAGE);

                // Libera recursos e encerra a aplicação
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
            // Fecha o EntityManagerFactory e recursos relacionados
            JpaConfig.getInstance().close();
            LOGGER.info("Recursos liberados com sucesso.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao liberar recursos", e);
        }
    }
}
