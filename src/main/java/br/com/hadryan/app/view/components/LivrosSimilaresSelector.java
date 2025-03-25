package br.com.hadryan.app.view.components;

import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.view.components.base.BaseTable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Componente refatorado para seleção de livros similares.
 */
public class LivrosSimilaresSelector extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextArea selectedBooksArea;
    private JButton selecionarButton;
    private JButton sugerirButton;

    private final Map<String, String> isbnToTitleMap = new HashMap<>();
    private final LivroController livroController;
    private final Window parentWindow;
    private Livro livroAtual;

    /**
     * Construtor do componente seletor de livros similares
     */
    public LivrosSimilaresSelector(Window parentWindow, LivroController livroController) {
        this.parentWindow = parentWindow;
        this.livroController = livroController;

        initComponents();
        layoutComponents();
        setupListeners();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        selectedBooksArea = new JTextArea();
        selectedBooksArea.setLineWrap(true);
        selectedBooksArea.setWrapStyleWord(true);
        selectedBooksArea.setEditable(true);
        selectedBooksArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));

        selecionarButton = new JButton("Selecionar Livros");
        selecionarButton.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        selecionarButton.setPreferredSize(new Dimension(150, 30));

        sugerirButton = new JButton("Sugerir Similares");
        sugerirButton.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        sugerirButton.setPreferredSize(new Dimension(150, 30));
    }

    /**
     * Configura o layout do componente
     */
    private void layoutComponents() {
        JScrollPane scrollPane = new JScrollPane(selectedBooksArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(500, 200));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.add(sugerirButton);
        buttonPanel.add(selecionarButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(500, 250));
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        selecionarButton.addActionListener(e -> mostrarDialogoSeletor());
        sugerirButton.addActionListener(e -> sugerirLivrosSimilares());

        selectedBooksArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                analisarEntradaManual();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                analisarEntradaManual();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                analisarEntradaManual();
            }
        });
    }

    /**
     * Exibe diálogo para seleção de livros similares
     */
    private void mostrarDialogoSeletor() {
        JDialog dialog;
        if (parentWindow instanceof JFrame) {
            dialog = new JDialog((JFrame)parentWindow, "Selecionar Livros Similares", true);
        } else if (parentWindow instanceof JDialog) {
            dialog = new JDialog((JDialog)parentWindow, "Selecionar Livros Similares", true);
        } else {
            dialog = new JDialog((Frame)null, "Selecionar Livros Similares", true);
            dialog.setLocationRelativeTo(parentWindow);
        }
        dialog.setLayout(new BorderLayout());

        String[] colunas = {"ID", "Título", "ISBN", "Autores"};
        BaseTable<Livro> livroTable = new BaseTable<>(colunas);

        livroTable.getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        livroTable.setData(livroController.listarTodos(), livro -> new Object[] {
                livro.getId(),
                livro.getTitulo(),
                livro.getIsbn(),
                livro.getAutores().stream()
                        .map(Autor::getNome)
                        .collect(Collectors.joining(", "))
        });

        JPanel buttonPanel = createButtonPanel(dialog, livroTable);

        dialog.add(new JScrollPane(livroTable.getTable()), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setVisible(true);
    }

    /**
     * Cria o painel de botões para o diálogo de seleção
     */
    private JPanel createButtonPanel(JDialog dialog, BaseTable<Livro> livroTable) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton adicionarButton = new JButton("Adicionar Selecionados");
        adicionarButton.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        adicionarButton.setPreferredSize(new Dimension(180, 30));

        JButton fecharButton = new JButton("Fechar");
        fecharButton.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        fecharButton.setPreferredSize(new Dimension(120, 30));

        adicionarButton.addActionListener(e -> {
            List<Livro> livrosSelecionados = livroTable.getSelectedItems();
            if (livrosSelecionados != null && !livrosSelecionados.isEmpty()) {
                int countAdded = 0;
                int countSkipped = 0;

                for (Livro livroSelecionado : livrosSelecionados) {
                    if (livroAtual != null && livroAtual.getId() != null &&
                            livroAtual.getId().equals(livroSelecionado.getId())) {
                        countSkipped++;
                        continue;
                    }

                    isbnToTitleMap.put(livroSelecionado.getIsbn(), livroSelecionado.getTitulo());
                    countAdded++;
                }

                atualizarAreaLivrosSelecionados();
                dialog.dispose();

                StringBuilder message = new StringBuilder();
                message.append(countAdded).append(" livro(s) adicionado(s) à lista de similares.");
                if (countSkipped > 0) {
                    message.append("\n").append(countSkipped).append(" livro(s) ignorado(s) por ser o mesmo livro que está sendo editado.");
                }

                JOptionPane.showMessageDialog(parentWindow,
                        message.toString(),
                        "Livros Adicionados",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Selecione pelo menos um livro.",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        fecharButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(adicionarButton);
        buttonPanel.add(fecharButton);

        return buttonPanel;
    }

    /**
     * Sugere livros similares com base no livro atual
     */
    private void sugerirLivrosSimilares() {
        if (livroAtual == null || livroAtual.getId() == null) {
            JOptionPane.showMessageDialog(parentWindow,
                    "É necessário salvar o livro antes de sugerir similares.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Livro> sugestoes = livroController.sugerirLivrosSimilares(livroAtual, 5);
        if (sugestoes.isEmpty()) {
            JOptionPane.showMessageDialog(parentWindow,
                    "Não foram encontradas sugestões de livros similares.",
                    "Informação",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (Livro sugestao : sugestoes) {
            isbnToTitleMap.put(sugestao.getIsbn(), sugestao.getTitulo());
        }

        atualizarAreaLivrosSelecionados();

        JOptionPane.showMessageDialog(parentWindow,
                "Foram adicionadas " + sugestoes.size() + " sugestões.",
                "Sugestões Adicionadas",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Atualiza a área de texto com os livros selecionados no formato {isbn} - {name}
     */
    private void atualizarAreaLivrosSelecionados() {
        StringBuilder sb = new StringBuilder();
        List<String> isbns = new ArrayList<>(isbnToTitleMap.keySet());
        Collections.sort(isbns);

        for (String isbn : isbns) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(isbn).append(" - ").append(isbnToTitleMap.get(isbn));
        }

        selectedBooksArea.setText(sb.toString());
    }

    /**
     * Define o livro atual
     */
    public void setLivroAtual(Livro livro) {
        this.livroAtual = livro;
        isbnToTitleMap.clear();

        if (livro != null && livro.getLivrosSimilares() != null) {
            for (Livro livroSimilar : livro.getLivrosSimilares()) {
                isbnToTitleMap.put(livroSimilar.getIsbn(), livroSimilar.getTitulo());
            }
        }

        atualizarAreaLivrosSelecionados();
    }

    /**
     * Obtém a lista de ISBNs dos livros similares selecionados
     */
    public List<String> getIsbnsSelcionados() {
        return new ArrayList<>(isbnToTitleMap.keySet());
    }

    /**
     * Analisa a entrada manual do usuário para atualizar o conjunto de ISBNs
     * e a associação de ISBNs a títulos quando disponível
     */
    private void analisarEntradaManual() {
        String texto = selectedBooksArea.getText().trim();
        Set<String> novosIsbns = new HashSet<>();

        if (!texto.isEmpty()) {
            String[] linhas = texto.split("\\n");
            for (String linha : linhas) {
                String isbn;
                if (linha.contains(" - ")) {
                    String[] partes = linha.split(" - ", 2);
                    isbn = partes[0].trim();
                    String titulo = partes.length > 1 ? partes[1].trim() : "";
                    if (!titulo.isEmpty()) {
                        isbnToTitleMap.put(isbn, titulo);
                    }
                } else if (linha.contains(",")) {
                    String[] isbns = linha.split(",");
                    for (String isbnItem : isbns) {
                        isbn = isbnItem.trim();
                        if (!isbn.isEmpty()) {
                            novosIsbns.add(isbn);
                            if (!isbnToTitleMap.containsKey(isbn)) {
                                buscarTituloPorIsbn(isbn);
                            }
                        }
                    }
                    continue;
                } else {
                    isbn = linha.trim();
                }

                if (!isbn.isEmpty()) {
                    novosIsbns.add(isbn);
                    if (!isbnToTitleMap.containsKey(isbn)) {
                        buscarTituloPorIsbn(isbn);
                    }
                }
            }
        }

        isbnToTitleMap.keySet().retainAll(novosIsbns);

        for (String isbn : novosIsbns) {
            if (!isbnToTitleMap.containsKey(isbn)) {
                isbnToTitleMap.put(isbn, "");
            }
        }
    }

    /**
     * Busca o título de um livro pelo ISBN no controlador
     */
    private void buscarTituloPorIsbn(String isbn) {
        livroController.buscarPorIsbn(isbn).ifPresent(livro -> {
            isbnToTitleMap.put(isbn, livro.getTitulo());
        });
    }

    /**
     * Adiciona um livro à lista de similares
     */
    public void adicionarLivro(Livro livro) {
        if (livro != null && livro.getIsbn() != null) {
            isbnToTitleMap.put(livro.getIsbn(), livro.getTitulo());
            atualizarAreaLivrosSelecionados();
        }
    }
}

