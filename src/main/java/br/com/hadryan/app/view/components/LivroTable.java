package br.com.hadryan.app.view.components;

import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Livro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Componente reutilizável para tabelas de livros.
 * Adaptado para tratar data de publicação como String.
 */
public class LivroTable extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTable livroTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;

    private Consumer<Livro> onDoubleClickAction;

    /**
     * Construtor do componente de tabela de livros
     */
    public LivroTable() {
        initComponents();
        layoutComponents();
        setupListeners();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        // Table model com células não editáveis
        tableModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Configura colunas da tabela
        tableModel.addColumn("ID");
        tableModel.addColumn("Título");
        tableModel.addColumn("Autores");
        tableModel.addColumn("ISBN");
        tableModel.addColumn("Editora");
        tableModel.addColumn("Data de Publicação");

        // Cria tabela
        livroTable = new JTable(tableModel);
        livroTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        livroTable.getTableHeader().setReorderingAllowed(false);

        // Configura a largura das colunas
        livroTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        livroTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Título
        livroTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Autores
        livroTable.getColumnModel().getColumn(3).setPreferredWidth(100); // ISBN
        livroTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Editora
        livroTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Data

        // ScrollPane para a tabela
        scrollPane = new JScrollPane(livroTable);
    }

    /**
     * Configura o layout do componente
     */
    private void layoutComponents() {
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        // Listener para duplo clique na tabela
        livroTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && livroTable.getSelectedRow() != -1 && onDoubleClickAction != null) {
                    Livro livroSelecionado = getLivroSelecionado();
                    if (livroSelecionado != null) {
                        onDoubleClickAction.accept(livroSelecionado);
                    }
                }
            }
        });
    }

    /**
     * Carrega os dados de livros na tabela
     */
    public void carregarDados(List<Livro> livros) {
        // Limpa dados existentes
        tableModel.setRowCount(0);

        // Adiciona livros à tabela
        for (Livro livro : livros) {
            // Formata autores como string separada por vírgulas
            String autores = livro.getAutores().stream()
                    .map(Autor::getNome)
                    .collect(Collectors.joining(", "));

            // Data de publicação como String diretamente
            String dataPublicacao = livro.getDataPublicacao() != null
                    ? livro.getDataPublicacao()
                    : "";

            // Adiciona linha à tabela
            tableModel.addRow(new Object[]{
                    livro.getId(),
                    livro.getTitulo(),
                    autores,
                    livro.getIsbn(),
                    livro.getEditora() != null ? livro.getEditora().getNome() : "",
                    dataPublicacao
            });
        }
    }

    /**
     * Retorna o livro selecionado na tabela
     */
    public Livro getLivroSelecionado() {
        int selectedRow = livroTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        // Retorna um objeto Livro com os dados básicos (apenas ID e ISBN)
        // Será necessário carregar o livro completo do repositório para ter todos os dados
        Livro livro = new Livro();
        livro.setId((Long) tableModel.getValueAt(selectedRow, 0));
        livro.setTitulo((String) tableModel.getValueAt(selectedRow, 1));
        livro.setIsbn((String) tableModel.getValueAt(selectedRow, 3));

        return livro;
    }

    /**
     * Retorna o índice da linha selecionada
     */
    public int getSelectedRow() {
        return livroTable.getSelectedRow();
    }

    /**
     * Define a ação a ser executada quando houver duplo clique em um livro
     */
    public void setOnDoubleClickAction(Consumer<Livro> action) {
        this.onDoubleClickAction = action;
    }

    /**
     * Define a seleção de linha na tabela
     */
    public void setSelectedRow(int row) {
        if (row >= 0 && row < livroTable.getRowCount()) {
            livroTable.setRowSelectionInterval(row, row);
        }
    }

    /**
     * Retorna a tabela de livros
     */
    public JTable getTable() {
        return livroTable;
    }

    /**
     * Retorna o modelo da tabela
     */
    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    /**
     * Define se a tabela está habilitada
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        livroTable.setEnabled(enabled);
    }
}
