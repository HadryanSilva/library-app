package br.com.hadryan.app.view.components.base;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Tabela base para exibição de dados
 */
public class BaseTable<T> extends JPanel {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JScrollPane scrollPane;
    private Consumer<T> onDoubleClickAction;
    private final Map<Integer, String> columnMap = new HashMap<>();
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

        for (int i = 0; i < columns.length; i++) {
            tableModel.addColumn(columns[i]);
            columnMap.put(i, columns[i]);
        }

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1 && onDoubleClickAction != null) {
                    onDoubleClickAction.accept(getSelectedItem());
                }
            }
        });

        // Cria o painel de rolagem
        scrollPane = new JScrollPane(table);
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
     * Obtém o índice da linha selecionada
     */
    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    /**
     * Obtém a tabela
     */
    public JTable getTable() {
        return table;
    }

    /**
     * Obtém o modelo da tabela
     */
    public DefaultTableModel getTableModel() {
        return tableModel;
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
