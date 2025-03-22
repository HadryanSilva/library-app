package br.com.hadryan.app.view.components;

import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.model.entity.Livro;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * Componente para seleção de livros similares.
 */
public class LivrosSimilaresSelector extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTextArea selectedBooksArea;
    private JButton selecionarButton;
    private JButton sugerirButton;

    private final Set<String> isbnsSet = new HashSet<>();
    private final LivroController livroController;
    private final JDialog parentDialog;
    private Livro livroAtual;

    /**
     * Construtor do componente seletor de livros similares
     */
    public LivrosSimilaresSelector(JDialog parentDialog, LivroController livroController) {
        this.parentDialog = parentDialog;
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

        // Área de texto para exibir ISBNs dos livros selecionados
        selectedBooksArea = new JTextArea(3, 30);
        selectedBooksArea.setLineWrap(true);
        selectedBooksArea.setWrapStyleWord(true);
        selectedBooksArea.setEditable(true);

        // Botões
        selecionarButton = new JButton("Selecionar Livros");
        sugerirButton = new JButton("Sugerir Similares");
    }

    /**
     * Configura o layout do componente
     */
    private void layoutComponents() {
        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sugerirButton);
        buttonPanel.add(selecionarButton);

        // Adiciona componentes ao painel principal
        add(new JScrollPane(selectedBooksArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        selecionarButton.addActionListener(e -> mostrarDialogoSeletor());
        sugerirButton.addActionListener(e -> sugerirLivrosSimilares());
    }

    /**
     * Exibe diálogo para seleção de livros similares
     */
    private void mostrarDialogoSeletor() {
        // Cria diálogo
        JDialog dialog = new JDialog(parentDialog, "Selecionar Livros Similares", true);
        dialog.setLayout(new BorderLayout());

        // Tabela de livros
        LivroTable livroTable = new LivroTable();
        livroTable.carregarDados(livroController.listarTodos());

        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton adicionarButton = new JButton("Adicionar");
        JButton fecharButton = new JButton("Fechar");

        // Adiciona action listeners para os botões
        adicionarButton.addActionListener(e -> {
            Livro livroSelecionado = livroTable.getLivroSelecionado();
            if (livroSelecionado != null) {
                // Não adiciona o livro atual como similar dele mesmo
                if (livroAtual != null && livroAtual.getId() != null &&
                        livroAtual.getId().equals(livroSelecionado.getId())) {
                    JOptionPane.showMessageDialog(dialog,
                            "Um livro não pode ser similar a ele mesmo.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Adiciona ISBN à lista se não estiver presente
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

        // Configura e exibe o diálogo
        dialog.add(new JScrollPane(livroTable), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(parentDialog);
        dialog.setVisible(true);
    }

    /**
     * Sugere livros similares com base no livro atual
     */
    private void sugerirLivrosSimilares() {
        if (livroAtual == null || livroAtual.getId() == null) {
            JOptionPane.showMessageDialog(parentDialog,
                    "É necessário salvar o livro antes de sugerir similares.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Livro> sugestoes = livroController.sugerirLivrosSimilares(livroAtual, 5);

        if (sugestoes.isEmpty()) {
            JOptionPane.showMessageDialog(parentDialog,
                    "Não foram encontradas sugestões de livros similares.",
                    "Informação",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Adiciona as sugestões à lista de selecionados
        for (Livro sugestao : sugestoes) {
            isbnsSet.add(sugestao.getIsbn());
        }

        atualizarAreaLivrosSelecionados();

        JOptionPane.showMessageDialog(parentDialog,
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

        // Limpa e atualiza os livros similares
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
        // Se o usuário digitou manualmente, precisamos atualizar o conjunto
        analisarEntradaManual();

        // Converte o conjunto para uma lista
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

        // Limpa o conjunto atual
        isbnsSet.clear();

        // Se o texto não estiver vazio, adiciona os ISBNs ao conjunto
        if (!texto.isEmpty()) {
            String[] isbnsArray = texto.split(",");
            for (String isbn : isbnsArray) {
                String isbnAjustado = isbn.trim();
                if (!isbnAjustado.isEmpty()) {
                    isbnsSet.add(isbnAjustado);
                }
            }
        }
    }
}