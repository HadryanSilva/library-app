package br.com.hadryan.app.view.components.base;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * Classe base para diálogos customizados
 */
public abstract class BaseDialog extends JDialog {

    private boolean confirmed = false;
    private final JPanel contentPanel;
    private final JPanel buttonPanel;

    /**
     * Construtor base para diálogos
     */
    public BaseDialog(Window parent, String title) {
        super(parent, title, ModalityType.APPLICATION_MODAL);

        contentPanel = new JPanel(new BorderLayout());
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(400, 300));
    }

    /**
     * Define o componente principal
     */
    protected void setMainComponent(JComponent component) {
        contentPanel.add(component, BorderLayout.CENTER);
    }

    /**
     * Adiciona um botão ao painel de botões
     */
    protected JButton addButton(String text, Consumer<ActionEvent> action) {
        JButton button = new JButton(text);
        button.addActionListener(action::accept);
        buttonPanel.add(button);
        return button;
    }

    /**
     * Marca o diálogo como confirmado e o fecha
     */
    protected void confirm() {
        confirmed = true;
        dispose();
    }

    /**
     * Cancela o diálogo e o fecha
     */
    protected void cancel() {
        confirmed = false;
        dispose();
    }

    /**
     * Verifica se o diálogo foi confirmado
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Exibe uma mensagem de erro
     */
    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Exibe uma mensagem de sucesso
     */
    protected void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exibe um diálogo de confirmação
     */
    protected boolean confirmAction(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirmação",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}