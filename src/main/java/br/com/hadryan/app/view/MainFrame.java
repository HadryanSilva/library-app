package br.com.hadryan.app.view;

import br.com.hadryan.app.LibraryApplication;
import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.service.importacao.ImportService;
import br.com.hadryan.app.view.components.panel.ImportacaoPanel;
import br.com.hadryan.app.view.components.panel.LivroListaPanel;
import br.com.hadryan.app.view.components.panel.LivroPesquisaPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Janela principal da aplicação.
 * Gerencia os painéis e controles da interface gráfica.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final String PAINEL_LISTA = "LISTA";
    public static final String PAINEL_PESQUISA = "PESQUISA";
    public static final String PAINEL_IMPORTACAO = "IMPORTACAO";

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private final Map<String, JPanel> paineis = new HashMap<>();
    private final Map<String, JButton> botoesNavegacao = new HashMap<>();

    private final LivroController livroController;
    private final ImportService importService;

    /**
     * Construtor da janela principal
     */
    public MainFrame(LivroController livroController, ImportService importService) {
        this.livroController = livroController;
        this.importService = importService;

        initComponents();
        setupLayout();
        setupToolbar();
        setupMenu();
        setupWindowListeners();

        mostrarPainel(PAINEL_LISTA);

        setTitle("Sistema de Gerenciamento de Biblioteca");
        setSize(900, 600);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        paineis.put(PAINEL_LISTA, new LivroListaPanel(this, livroController));
        paineis.put(PAINEL_PESQUISA, new LivroPesquisaPanel(this, livroController));
        paineis.put(PAINEL_IMPORTACAO, new ImportacaoPanel(this, importService));
    }

    /**
     * Configura o layout da janela
     */
    private void setupLayout() {
        for (Map.Entry<String, JPanel> entry : paineis.entrySet()) {
            contentPanel.add(entry.getValue(), entry.getKey());
        }

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Configura a barra de ferramentas
     */
    private void setupToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        JButton listaButton = createToolbarButton("Lista de Livros", PAINEL_LISTA);
        JButton pesquisaButton = createToolbarButton("Pesquisar", PAINEL_PESQUISA);
        JButton importacaoButton = createToolbarButton("Importar", PAINEL_IMPORTACAO);

        toolBar.add(listaButton);
        toolBar.addSeparator();
        toolBar.add(pesquisaButton);
        toolBar.addSeparator();
        toolBar.add(importacaoButton);

        getContentPane().add(toolBar, BorderLayout.NORTH);
    }

    /**
     * Cria um botão para a barra de ferramentas
     */
    private JButton createToolbarButton(String text, String painelId) {
        JButton button = new JButton(text);
        button.setToolTipText("Exibir " + text.toLowerCase());
        button.addActionListener(e -> mostrarPainel(painelId));
        botoesNavegacao.put(painelId, button);
        return button;
    }

    /**
     * Configura o menu da aplicação
     */
    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Arquivo");
        JMenuItem exitItem = new JMenuItem("Sair");
        exitItem.addActionListener(e -> sair());
        fileMenu.add(exitItem);

        JMenu booksMenu = new JMenu("Livros");

        JMenuItem listItem = new JMenuItem("Lista de Livros");
        listItem.addActionListener(e -> mostrarPainel(PAINEL_LISTA));

        JMenuItem searchItem = new JMenuItem("Pesquisar Livros");
        searchItem.addActionListener(e -> mostrarPainel(PAINEL_PESQUISA));

        JMenuItem importItem = new JMenuItem("Importar Livros");
        importItem.addActionListener(e -> mostrarPainel(PAINEL_IMPORTACAO));

        booksMenu.add(listItem);
        booksMenu.add(searchItem);
        booksMenu.addSeparator();
        booksMenu.add(importItem);

        JMenu helpMenu = new JMenu("Ajuda");
        JMenuItem aboutItem = new JMenuItem("Sobre");
        aboutItem.addActionListener(e -> mostrarSobre());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(booksMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Configura os listeners da janela
     */
    private void setupWindowListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sair();
            }
        });
    }

    /**
     * Mostra um painel específico no layout de cartões
     */
    public void mostrarPainel(String nomePainel) {
        if (nomePainel.equals(PAINEL_LISTA)) {
            ((LivroListaPanel) paineis.get(PAINEL_LISTA)).updateData();
        }

        cardLayout.show(contentPanel, nomePainel);

        for (Map.Entry<String, JButton> entry : botoesNavegacao.entrySet()) {
            entry.getValue().setEnabled(!entry.getKey().equals(nomePainel));
        }
    }

    /**
     * Exibe o diálogo "Sobre"
     */
    private void mostrarSobre() {
        JOptionPane.showMessageDialog(this,
                "Sistema de Gerenciamento de Biblioteca\n" +
                        "Versão 1.0\n\n" +
                        "Desenvolvido como parte do processo de avaliação",
                "Sobre",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Encerra a aplicação
     */
    private void sair() {
        int opcao = JOptionPane.showConfirmDialog(this,
                "Deseja realmente sair da aplicação?",
                "Confirmação",
                JOptionPane.YES_NO_OPTION);

        if (opcao == JOptionPane.YES_OPTION) {
            LibraryApplication.liberarRecursos();
            dispose();
            System.exit(0);
        }
    }

    /**
     * Atualiza a lista de livros
     */
    public void atualizarListaLivros() {
        ((LivroListaPanel) paineis.get(PAINEL_LISTA)).updateData();
    }

    /**
     * Retorna o controlador de livros
     */
    public LivroController getLivroController() {
        return livroController;
    }
}