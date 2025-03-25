package br.com.hadryan.app.view.components.base;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Tabela base para exibição de dados
 *
 * @author Hadryan Silva
 * @since 23-03-2025
 */
public class BaseTable<T> extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private Consumer<T> onDoubleClickAction;
    private final List<T> data = new ArrayList<>();

    /**
     * Construtor da tabela melhorada
     */
    public BaseTable(String[] columns) {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (String column : columns) {
            tableModel.addColumn(column);
        }

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        table.setRowHeight(28);
        table.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font(Font.DIALOG, Font.BOLD, 14));

        if (table.getColumnCount() >= 5) {
            table.getColumnModel().getColumn(0).setPreferredWidth(60);
            table.getColumnModel().getColumn(1).setPreferredWidth(300);
            table.getColumnModel().getColumn(2).setPreferredWidth(150);
            table.getColumnModel().getColumn(3).setPreferredWidth(100);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);
        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1 && onDoubleClickAction != null) {
                    onDoubleClickAction.accept(getSelectedItem());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Define a ação de duplo clique
     */
    public void setOnDoubleClickAction(Consumer<T> action) {
        this.onDoubleClickAction = action;
    }

    /**
     * Carrega dados na tabela
     */
    public void setData(List<T> items, Function<T, Object[]> rowMapper) {
        tableModel.setRowCount(0);
        data.clear();

        for (T item : items) {
            tableModel.addRow(rowMapper.apply(item));
            data.add(item);
        }
    }

    /**
     * Obtém o item selecionado
     */
    public T getSelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1 && selectedRow < data.size()) {
            return data.get(selectedRow);
        }
        return null;
    }

    /**
     * Obtém todos os itens selecionados (seleção múltipla)
     */
    public List<T> getSelectedItems() {
        int[] selectedRows = table.getSelectedRows();
        List<T> selectedItems = new ArrayList<>();

        for (int row : selectedRows) {
            if (row < data.size()) {
                selectedItems.add(data.get(row));
            }
        }

        return selectedItems;
    }

    /**
     * Obtém o modelo da tabela
     */
    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    /**
     * Obtém a tabela JTable
     */
    public JTable getTable() {
        return table;
    }

    /**
     * Define o listener de seleção
     */
    public void addSelectionListener(Consumer<T> listener) {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                T selectedItem = getSelectedItem();
                if (selectedItem != null) {
                    listener.accept(selectedItem);
                }
            }
        });
    }
}
