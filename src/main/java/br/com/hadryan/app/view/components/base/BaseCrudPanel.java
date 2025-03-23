package br.com.hadryan.app.view.components.base;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * Classe base para painéis CRUD
 */
public abstract class BaseCrudPanel extends JPanel {

    protected final JPanel toolbarPanel;
    protected final JPanel contentPanel;
    protected final JPanel buttonPanel;

    /**
     * Construtor do painel CRUD
     */
    public BaseCrudPanel() {
        setLayout(new BorderLayout());

        toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentPanel = new JPanel(new BorderLayout());
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        add(toolbarPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Adiciona um botão à barra de ferramentas
     */
    protected JButton addToolbarButton(String text, Consumer<ActionEvent> action) {
        JButton button = new JButton(text);
        button.addActionListener(action::accept);
        toolbarPanel.add(button);
        return button;
    }

    /**
     * Adiciona um botão ao painel de botões
     */
    protected JButton addActionButton(String text, Consumer<ActionEvent> action) {
        JButton button = new JButton(text);
        button.addActionListener(action::accept);
        buttonPanel.add(button);
        return button;
    }

    /**
     * Define o componente principal
     */
    protected void setMainComponent(JComponent component) {
        contentPanel.add(component, BorderLayout.CENTER);
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

    /**
     * Método abstrato para atualizar dados
     */
    public abstract void updateData();
}
