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

    private final Set<String> isbnsSet = new HashSet<>();
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

        selectedBooksArea = new JTextArea(3, 30);
        selectedBooksArea.setLineWrap(true);
        selectedBooksArea.setWrapStyleWord(true);
        selectedBooksArea.setEditable(true);

        selecionarButton = new JButton("Selecionar Livros");
        sugerirButton = new JButton("Sugerir Similares");
    }

    /**
     * Configura o layout do componente
     */
    private void layoutComponents() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sugerirButton);
        buttonPanel.add(selecionarButton);

        add(new JScrollPane(selectedBooksArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
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

        livroTable.setData(livroController.listarTodos(), livro -> new Object[] {
                livro.getId(),
                livro.getTitulo(),
                livro.getIsbn(),
                livro.getAutores().stream()
                        .map(Autor::getNome)
                        .collect(Collectors.joining(", "))
        });

        JPanel buttonPanel = createButtonPanel(dialog, livroTable);

        dialog.add(new JScrollPane(livroTable), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setVisible(true);
    }

    /**
     * Cria o painel de botões para o diálogo de seleção
     */
    private JPanel createButtonPanel(JDialog dialog, BaseTable<Livro> livroTable) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton adicionarButton = new JButton("Adicionar");
        JButton fecharButton = new JButton("Fechar");

        adicionarButton.addActionListener(e -> {
            Livro livroSelecionado = livroTable.getSelectedItem();
            if (livroSelecionado != null) {
                if (livroAtual != null && livroAtual.getId() != null &&
                        livroAtual.getId().equals(livroSelecionado.getId())) {
                    JOptionPane.showMessageDialog(dialog,
                            "Um livro não pode ser similar a ele mesmo.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (isbnsSet.add(livroSelecionado.getIsbn())) {
                    atualizarAreaLivrosSelecionados();
                }
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Selecione um livro.",
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
            isbnsSet.add(sugestao.getIsbn());
        }

        atualizarAreaLivrosSelecionados();

        JOptionPane.showMessageDialog(parentWindow,
                "Foram adicionadas " + sugestoes.size() + " sugestões.",
                "Sugestões Adicionadas",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Atualiza a área de texto com os ISBNs selecionados
     */
    private void atualizarAreaLivrosSelecionados() {
        selectedBooksArea.setText(String.join(", ", isbnsSet));
    }

    /**
     * Define o livro atual
     */
    public void setLivroAtual(Livro livro) {
        this.livroAtual = livro;
        isbnsSet.clear();

        if (livro != null && livro.getLivrosSimilares() != null) {
            for (Livro livroSimilar : livro.getLivrosSimilares()) {
                isbnsSet.add(livroSimilar.getIsbn());
            }
        }

        atualizarAreaLivrosSelecionados();
    }

    /**
     * Obtém a lista de ISBNs dos livros similares selecionados
     */
    public List<String> getIsbnsSelcionados() {
        return new ArrayList<>(isbnsSet);
    }

    /**
     * Obtém o texto da área de seleção
     */
    public String getTextoLivrosSelecionados() {
        return selectedBooksArea.getText();
    }

    /**
     * Define o texto da área de seleção
     */
    public void setTextoLivrosSelecionados(String texto) {
        selectedBooksArea.setText(texto);
        analisarEntradaManual();
    }

    /**
     * Analisa a entrada manual do usuário para atualizar o conjunto de ISBNs
     */
    private void analisarEntradaManual() {
        String texto = selectedBooksArea.getText().trim();

        isbnsSet.clear();

        if (!texto.isEmpty()) {
            Arrays.stream(texto.split(","))
                    .map(String::trim)
                    .filter(isbn -> !isbn.isEmpty())
                    .forEach(isbnsSet::add);
        }
    }
}