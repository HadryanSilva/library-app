package br.com.hadryan.app.view.components.base;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * Classe base para diálogos customizados
 *
 * @author Hadryan Silva
 * @since 23-03-2025
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

        setLayout(new BorderLayout());

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(450, 300));
    }

    /**
     * Define o componente principal, que ocupará todo o espaço central
     */
    protected void setMainComponent(JComponent component) {
        contentPanel.removeAll();
        contentPanel.add(component, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Adiciona um botão ao painel de botões
     */
    protected JButton addButton(String text, Consumer<ActionEvent> action) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(120, 30));
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
}