package br.com.hadryan.app.view;

import br.com.hadryan.app.LibraryApplication;
import br.com.hadryan.app.util.MessageUtil;
import br.com.hadryan.app.util.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

/**
 * Janela principal da aplicação.
 * Gerencia os painéis e controles da interface gráfica.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    // Layout e painéis
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Referências aos painéis
    private BookListPanel bookListPanel;
    private BookSearchPanel bookSearchPanel;
    private ImportPanel importPanel;

    // Componentes da UI
    private JToolBar toolBar;
    private JButton listButton;
    private JButton searchButton;
    private JButton importButton;

    /**
     * Construtor da janela principal
     */
    public MainFrame() {
        initComponents();
        setupLayout();
        setupToolbar();
        setupMenu();
        setupWindowListeners();

        // Exibe o painel inicial
        showPanel("LIST");
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        setTitle("Sistema de Gerenciamento de Biblioteca");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(800, 500));
        UIUtil.centerOnScreen(this);

        // Inicializa o layout de cartões para troca de painéis
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Inicializa os painéis
        bookListPanel = new BookListPanel(this);
        bookSearchPanel = new BookSearchPanel(this);
        importPanel = new ImportPanel(this);

        // Inicializa a barra de ferramentas
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        // Botões da barra de ferramentas
        listButton = new JButton("Lista de Livros");
        searchButton = new JButton("Pesquisar");
        importButton = new JButton("Importar");
    }

    /**
     * Configura o layout da janela
     */
    private void setupLayout() {
        // Adiciona painéis ao layout de cartões
        contentPanel.add(bookListPanel, "LIST");
        contentPanel.add(bookSearchPanel, "SEARCH");
        contentPanel.add(importPanel, "IMPORT");

        // Adiciona o painel de conteúdo à janela
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Configura a barra de ferramentas
     */
    private void setupToolbar() {
        // Configura os botões
        listButton.setToolTipText("Exibir lista de livros");
        searchButton.setToolTipText("Pesquisar livros");
        importButton.setToolTipText("Importar livros");

        // Adiciona ações aos botões
        listButton.addActionListener(e -> showPanel("LIST"));
        searchButton.addActionListener(e -> showPanel("SEARCH"));
        importButton.addActionListener(e -> showPanel("IMPORT"));

        // Adiciona botões à barra de ferramentas
        toolBar.add(listButton);
        toolBar.addSeparator();
        toolBar.add(searchButton);
        toolBar.addSeparator();
        toolBar.add(importButton);
    }

    /**
     * Configura o menu da aplicação
     */
    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Menu Arquivo
        JMenu fileMenu = new JMenu("Arquivo");

        JMenuItem exitItem = new JMenuItem("Sair");
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(exitItem);

        // Menu Livros
        JMenu booksMenu = new JMenu("Livros");

        JMenuItem listItem = new JMenuItem("Lista de Livros");
        listItem.addActionListener(e -> showPanel("LIST"));

        JMenuItem searchItem = new JMenuItem("Pesquisar Livros");
        searchItem.addActionListener(e -> showPanel("SEARCH"));

        JMenuItem importItem = new JMenuItem("Importar Livros");
        importItem.addActionListener(e -> showPanel("IMPORT"));

        booksMenu.add(listItem);
        booksMenu.add(searchItem);
        booksMenu.addSeparator();
        booksMenu.add(importItem);

        // Menu Ajuda
        JMenu helpMenu = new JMenu("Ajuda");

        JMenuItem aboutItem = new JMenuItem("Sobre");
        aboutItem.addActionListener(e -> showAboutDialog());

        helpMenu.add(aboutItem);

        // Adiciona menus à barra de menu
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
                exitApplication();
            }
        });
    }

    /**
     * Mostra um painel específico no layout de cartões
     *
     * @param panelName Nome do painel a ser mostrado
     */
    public void showPanel(String panelName) {
        // Atualiza dados se necessário
        if (panelName.equals("LIST")) {
            bookListPanel.refreshData();
        }

        // Exibe o painel
        cardLayout.show(contentPanel, panelName);

        // Atualiza a barra de ferramentas para destacar o botão ativo
        listButton.setEnabled(!panelName.equals("LIST"));
        searchButton.setEnabled(!panelName.equals("SEARCH"));
        importButton.setEnabled(!panelName.equals("IMPORT"));
    }

    /**
     * Exibe o diálogo "Sobre"
     */
    private void showAboutDialog() {
        StringBuilder message = new StringBuilder();
        message.append("Sistema de Gerenciamento de Biblioteca\n");
        message.append("Versão 1.0\n\n");
        message.append("Desenvolvido por Hadryan Silva\n");

        MessageUtil.showInfo(this, message.toString(), "Sobre");
    }

    /**
     * Encerra a aplicação
     */
    private void exitApplication() {
        boolean confirm = MessageUtil.showConfirm(this,
                "Deseja realmente sair da aplicação?",
                "Confirmação");

        if (confirm) {
            // Fecha recursos
            LibraryApplication.cleanupResources();

            // Encerra a aplicação
            dispose();
            System.exit(0);
        }
    }
}
