package br.com.hadryan.app.view.components.base;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Componente de formulário reutilizável que simplifica a criação e gerenciamento
 * de formulários com layout padronizado.
 *
 * @author Hadryan Silva
 * @since 23-03-2025
 */
public class FormPanel extends JPanel {

    private final GridBagConstraints gbc;
    private int currentRow = 0;
    private final Map<String, JComponent> fields = new HashMap<>();

    /**
     * Construtor do painel de formulário com layout GridBag
     */
    public FormPanel() {
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        setBackground(UIManager.getColor("Panel.background"));
    }

    /**
     * Adiciona um campo de formulário com label
     *
     * @param labelText Texto do label
     * @param field Componente de entrada
     * @param fieldName Nome do campo para referência futura
     * @return O componente adicionado
     */
    public <T extends JComponent> T addField(String labelText, T field, String fieldName) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.DIALOG, Font.BOLD, 14));

        gbc.gridx = 0;
        gbc.gridy = currentRow;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(field, gbc);

        fields.put(fieldName, field);
        currentRow++;

        return field;
    }

    /**
     * Adiciona um campo com label e um componente adicional na mesma linha
     */
    public <T extends JComponent> T addFieldWithComponent(String labelText, T field, String fieldName, JComponent extra) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.DIALOG, Font.BOLD, 14));

        gbc.gridx = 0;
        gbc.gridy = currentRow;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.8;
        add(field, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.2;
        add(extra, gbc);

        fields.put(fieldName, field);
        currentRow++;

        return field;
    }

    /**
     * Adiciona uma área de texto com label
     */
    public JTextArea addTextArea(String labelText, String fieldName, int rows, int cols) {
        JTextArea textArea = new JTextArea(rows, cols);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.DIALOG, Font.BOLD, 14));

        gbc.gridx = 0;
        gbc.gridy = currentRow;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = rows > 5 ? 1.0 : 0.0;
        gbc.gridwidth = 2;
        add(scrollPane, gbc);
        gbc.gridwidth = 1;
        gbc.weighty = 0.0;

        fields.put(fieldName, textArea);
        currentRow++;

        return textArea;
    }

    /**
     * Adiciona um componente que ocupa toda a largura
     */
    public <T extends JComponent> T addFullWidthComponent(T component, String fieldName) {
        gbc.gridx = 0;
        gbc.gridy = currentRow;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        if (component instanceof JScrollPane ||
                component instanceof JPanel ||
                (component instanceof JTextArea && ((JTextArea)component).getRows() > 5)) {
            gbc.weighty = 1.0;
        } else {
            gbc.weighty = 0.0;
        }

        gbc.gridwidth = 3;
        add(component, gbc);
        gbc.gridwidth = 1;
        gbc.weighty = 0.0; // Reseta o peso vertical

        if (fieldName != null) {
            fields.put(fieldName, component);
        }
        currentRow++;

        return component;
    }

    /**
     * Define o valor de um campo de texto
     */
    public void setTextField(String fieldName, String value) {
        JComponent component = fields.get(fieldName);
        if (component instanceof JTextField) {
            ((JTextField) component).setText(value != null ? value : "");
        } else if (component instanceof JTextArea) {
            ((JTextArea) component).setText(value != null ? value : "");
        }
    }

    /**
     * Obtém o valor de um campo de texto
     */
    public String getTextFieldValue(String fieldName) {
        JComponent component = fields.get(fieldName);
        if (component instanceof JTextField) {
            return ((JTextField) component).getText();
        } else if (component instanceof JTextArea) {
            return ((JTextArea) component).getText();
        }
        return null;
    }

    /**
     * Obtém um campo do formulário pelo nome
     *
     * @param fieldName Nome do campo
     * @return O componente associado ao nome, ou null se não existir
     */
    public JComponent getField(String fieldName) {
        return fields.get(fieldName);
    }

}
