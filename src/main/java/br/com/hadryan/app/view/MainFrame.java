package br.com.hadryan.app.view;

import br.com.hadryan.app.LibraryApplication;
import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.service.importacao.ImportService;
import br.com.hadryan.app.view.components.ImportacaoPainel;
import br.com.hadryan.app.view.components.LivroListaPainel;
import br.com.hadryan.app.view.components.LivroPesquisaPainel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Janela principal da aplicação.
 * Gerencia os painéis e controles da interface gráfica.
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // Layout e painéis
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Referências aos painéis
    private LivroListaPainel livroListaPainel;
    private LivroPesquisaPainel livroPesquisaPainel;
    private ImportacaoPainel importacaoPainel;

    // Barra de ferramentas e botões
    private JToolBar toolBar;
    private JButton listaButton;
    private JButton pesquisaButton;
    private JButton importacaoButton;

    // Controladores e serviços
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

        // Exibe o painel inicial
        mostrarPainel("LISTA");

        // Configura a janela
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
        // Inicializa o layout de cartões para troca de painéis
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Inicializa os painéis
        livroListaPainel = new LivroListaPainel(this, livroController);
        livroPesquisaPainel = new LivroPesquisaPainel(this, livroController);
        importacaoPainel = new ImportacaoPainel(this, importService);

        // Inicializa a barra de ferramentas
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        // Botões da barra de ferramentas
        listaButton = new JButton("Lista de Livros");
        pesquisaButton = new JButton("Pesquisar");
        importacaoButton = new JButton("Importar");
    }

    /**
     * Configura o layout da janela
     */
    private void setupLayout() {
        // Adiciona painéis ao layout de cartões
        contentPanel.add(livroListaPainel, "LISTA");
        contentPanel.add(livroPesquisaPainel, "PESQUISA");
        contentPanel.add(importacaoPainel, "IMPORTACAO");

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
        listaButton.setToolTipText("Exibir lista de livros");
        pesquisaButton.setToolTipText("Pesquisar livros");
        importacaoButton.setToolTipText("Importar livros");

        // Adiciona ações aos botões
        listaButton.addActionListener(e -> mostrarPainel("LISTA"));
        pesquisaButton.addActionListener(e -> mostrarPainel("PESQUISA"));
        importacaoButton.addActionListener(e -> mostrarPainel("IMPORTACAO"));

        // Adiciona botões à barra de ferramentas
        toolBar.add(listaButton);
        toolBar.addSeparator();
        toolBar.add(pesquisaButton);
        toolBar.addSeparator();
        toolBar.add(importacaoButton);
    }

    /**
     * Configura o menu da aplicação
     */
    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Menu Arquivo
        JMenu fileMenu = new JMenu("Arquivo");

        JMenuItem exitItem = new JMenuItem("Sair");
        exitItem.addActionListener(e -> sair());

        fileMenu.add(exitItem);

        // Menu Livros
        JMenu booksMenu = new JMenu("Livros");

        JMenuItem listItem = new JMenuItem("Lista de Livros");
        listItem.addActionListener(e -> mostrarPainel("LISTA"));

        JMenuItem searchItem = new JMenuItem("Pesquisar Livros");
        searchItem.addActionListener(e -> mostrarPainel("PESQUISA"));

        JMenuItem importItem = new JMenuItem("Importar Livros");
        importItem.addActionListener(e -> mostrarPainel("IMPORTACAO"));

        booksMenu.add(listItem);
        booksMenu.add(searchItem);
        booksMenu.addSeparator();
        booksMenu.add(importItem);

        // Menu Ajuda
        JMenu helpMenu = new JMenu("Ajuda");

        JMenuItem aboutItem = new JMenuItem("Sobre");
        aboutItem.addActionListener(e -> mostrarSobre());

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
                sair();
            }
        });
    }

    /**
     * Mostra um painel específico no layout de cartões
     */
    public void mostrarPainel(String nomePainel) {
        // Atualiza dados se necessário
        if (nomePainel.equals("LISTA")) {
            livroListaPainel.atualizarDados();
        }

        // Exibe o painel
        cardLayout.show(contentPanel, nomePainel);

        // Atualiza a barra de ferramentas para destacar o botão ativo
        listaButton.setEnabled(!nomePainel.equals("LISTA"));
        pesquisaButton.setEnabled(!nomePainel.equals("PESQUISA"));
        importacaoButton.setEnabled(!nomePainel.equals("IMPORTACAO"));
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
            // Libera recursos
            LibraryApplication.liberarRecursos();

            // Encerra a aplicação
            dispose();
            System.exit(0);
        }
    }

    /**
     * Atualiza a lista de livros
     */
    public void atualizarListaLivros() {
        livroListaPainel.atualizarDados();
    }

    /**
     * Retorna o controlador de livros
     */
    public LivroController getLivroController() {
        return livroController;
    }
}