package br.com.hadryan.app.view.components.validator;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilitária para validação de campos de formulário.
 *
 * @author Hadryan Silva
 * @since 02-04-2025
 */
public class FormValidator {

    private final Map<JComponent, String> requiredFields = new HashMap<>();
    private final Map<JComponent, Border> originalBorders = new HashMap<>();
    private final Color errorColor = new Color(255, 235, 235);
    private final Border errorBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.RED),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
    );

    /**
     * Adiciona um campo obrigatório à validação
     *
     * @param component Componente a ser validado
     * @param fieldName Nome do campo para exibição em mensagens de erro
     * @return Esta instância para encadeamento de métodos
     */
    public FormValidator addRequiredField(JComponent component, String fieldName) {
        requiredFields.put(component, fieldName);
        originalBorders.put(component, component.getBorder());
        return this;
    }

    /**
     * Valida todos os campos registrados
     *
     * @return true se todos os campos são válidos, false caso contrário
     */
    public boolean validateAll() {
        clearErrors();

        boolean isValid = true;

        for (Map.Entry<JComponent, String> entry : requiredFields.entrySet()) {
            JComponent component = entry.getKey();

            if (validateComponent(component)) {
                markAsError(component);
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Limpa os erros de todos os campos
     */
    public void clearErrors() {
        for (Map.Entry<JComponent, Border> entry : originalBorders.entrySet()) {
            JComponent component = entry.getKey();
            Border originalBorder = entry.getValue();

            component.setBorder(originalBorder);
            if (component instanceof JTextComponent) {
                component.setBackground(UIManager.getColor("TextField.background"));
            }
        }
    }

    /**
     * Obtém a mensagem de erro com a lista de campos não preenchidos
     *
     * @return Mensagem formatada com os erros
     */
    public String getErrorMessage() {
        StringBuilder errorMessage = new StringBuilder("Os seguintes campos são obrigatórios:\n");

        boolean hasErrors = false;
        for (Map.Entry<JComponent, String> entry : requiredFields.entrySet()) {
            JComponent component = entry.getKey();
            String fieldName = entry.getValue();

            if (validateComponent(component)) {
                errorMessage.append("- ").append(fieldName).append("\n");
                hasErrors = true;
            }
        }

        return hasErrors ? errorMessage.toString() : "";
    }

    /**
     * Valida um componente específico
     */
    private boolean validateComponent(JComponent component) {
        if (component instanceof JTextComponent) {
            return ((JTextComponent) component).getText().trim().isEmpty();
        } else if (component instanceof JComboBox) {
            return ((JComboBox<?>) component).getSelectedItem() == null;
        }

        return false;
    }

    /**
     * Marca um componente como erro
     */
    private void markAsError(JComponent component) {
        component.setBorder(errorBorder);
        if (component instanceof JTextComponent) {
            component.setBackground(errorColor);
        }
    }
}
