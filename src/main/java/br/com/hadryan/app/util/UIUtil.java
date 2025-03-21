package br.com.hadryan.app.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Classe utilitária para operações relacionadas à interface de usuário.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class UIUtil {

    /**
     * Configura a aparência da aplicação
     */
    public static void setupLookAndFeel() {
        try {
            // Configura o Look and Feel para o padrão do sistema
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Customiza componentes globais
            customizeGlobalUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Customiza componentes globais da UI
     */
    private static void customizeGlobalUI() {
        // Define uma fonte padrão para todos os componentes
        // Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
        // UIManager.put("Button.font", defaultFont);
        // UIManager.put("Label.font", defaultFont);
        // UIManager.put("TextField.font", defaultFont);
        // UIManager.put("TextArea.font", defaultFont);
        // UIManager.put("ComboBox.font", defaultFont);
        // UIManager.put("Table.font", defaultFont);
        // UIManager.put("TableHeader.font", defaultFont);

        // Espaçamento padrão para painéis
        UIManager.put("Panel.border", new EmptyBorder(5, 5, 5, 5));
    }

    /**
     * Cria um JPanel com BorderLayout e margens
     *
     * @return JPanel configurado
     */
    public static JPanel createStandardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }

    /**
     * Cria um painel de botões alinhado à direita
     *
     * @param buttons Botões a serem adicionados
     * @return Painel configurado
     */
    public static JPanel createButtonPanel(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        for (JButton button : buttons) {
            panel.add(button);
        }
        return panel;
    }

    /**
     * Cria um painel com layout de grade
     *
     * @return Painel configurado com GridBagLayout
     */
    public static JPanel createGridPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }

    /**
     * Cria um rótulo com texto em negrito
     *
     * @param text Texto do rótulo
     * @return Rótulo configurado
     */
    public static JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        Font font = label.getFont();
        label.setFont(font.deriveFont(Font.BOLD));
        return label;
    }

    /**
     * Configura uma tabela com aparência padrão
     *
     * @param table Tabela a ser configurada
     */
    public static void setupStandardTable(JTable table) {
        table.setRowHeight(25);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    /**
     * Centraliza um componente na tela
     *
     * @param component Componente a ser centralizado
     */
    public static void centerOnScreen(Component component) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension componentSize = component.getSize();
        int x = (screenSize.width - componentSize.width) / 2;
        int y = (screenSize.height - componentSize.height) / 2;
        component.setLocation(x, y);
    }

    /**
     * Centraliza um componente em relação a outro componente
     *
     * @param child Componente a ser centralizado
     * @param parent Componente de referência
     */
    public static void centerOnComponent(Component child, Component parent) {
        Dimension childSize = child.getSize();
        Point parentLocation = parent.getLocationOnScreen();
        Dimension parentSize = parent.getSize();

        int x = parentLocation.x + (parentSize.width - childSize.width) / 2;
        int y = parentLocation.y + (parentSize.height - childSize.height) / 2;

        child.setLocation(x, y);
    }
}
