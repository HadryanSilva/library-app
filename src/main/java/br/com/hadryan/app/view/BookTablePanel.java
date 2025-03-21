package br.com.hadryan.app.view;

import br.com.hadryan.app.model.entity.Author;
import br.com.hadryan.app.model.entity.Book;
import br.com.hadryan.app.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Componente reutilizável para tabelas de livros.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class BookTablePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;

    private Consumer<Book> onDoubleClickAction;

    /**
     * Construtor do componente de tabela de livros
     */
    public BookTablePanel() {
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
        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getTableHeader().setReorderingAllowed(false);

        // Configura a largura das colunas
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Título
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Autores
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(100); // ISBN
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Editora
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Data

        // ScrollPane para a tabela
        scrollPane = new JScrollPane(bookTable);
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
        bookTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1 && onDoubleClickAction != null) {
                    Book selectedBook = getSelectedBook();
                    if (selectedBook != null) {
                        onDoubleClickAction.accept(selectedBook);
                    }
                }
            }
        });
    }

    /**
     * Carrega os dados de livros na tabela
     *
     * @param books Lista de livros a serem exibidos
     */
    public void loadData(List<Book> books) {
        // Limpa dados existentes
        tableModel.setRowCount(0);

        // Adiciona livros à tabela
        for (Book book : books) {
            // Formata autores como string separada por vírgulas
            String authors = book.getAuthors().stream()
                    .map(Author::getName)
                    .collect(Collectors.joining(", "));

            // Formata data de publicação
            String pubDate = DateUtil.format(book.getPublicationDate());

            // Adiciona linha à tabela
            tableModel.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    authors,
                    book.getIsbn(),
                    book.getPublisher() != null ? book.getPublisher().getName() : "",
                    pubDate
            });
        }
    }

    /**
     * Retorna o livro selecionado na tabela
     *
     * @return O livro selecionado, ou null se nenhum estiver selecionado
     */
    public Book getSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        // Retorna um objeto Book com os dados básicos (apenas ID e ISBN)
        // Será necessário carregar o livro completo do repositório para ter todos os dados
        Book book = new Book();
        book.setId((Long) tableModel.getValueAt(selectedRow, 0));
        book.setTitle((String) tableModel.getValueAt(selectedRow, 1));
        book.setIsbn((String) tableModel.getValueAt(selectedRow, 3));

        return book;
    }

    /**
     * Retorna o índice da linha selecionada
     *
     * @return O índice da linha selecionada, ou -1 se nenhuma estiver selecionada
     */
    public int getSelectedRow() {
        return bookTable.getSelectedRow();
    }

    /**
     * Define a ação a ser executada quando houver duplo clique em um livro
     *
     * @param action Consumer que recebe o livro selecionado
     */
    public void setOnDoubleClickAction(Consumer<Book> action) {
        this.onDoubleClickAction = action;
    }

    /**
     * Define a seleção de linha na tabela
     *
     * @param row Índice da linha a ser selecionada
     */
    public void setSelectedRow(int row) {
        if (row >= 0 && row < bookTable.getRowCount()) {
            bookTable.setRowSelectionInterval(row, row);
        }
    }

    /**
     * Retorna a tabela de livros
     *
     * @return A tabela JTable
     */
    public JTable getTable() {
        return bookTable;
    }

    /**
     * Retorna o modelo da tabela
     *
     * @return O modelo DefaultTableModel
     */
    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    /**
     * Define se a tabela está habilitada
     *
     * @param enabled true para habilitar, false para desabilitar
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        bookTable.setEnabled(enabled);
    }
}
