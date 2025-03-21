package br.com.hadryan.app.util;

import javax.swing.*;
import java.awt.*;

/**
 * Classe utilitária para exibição de mensagens na interface.
 *
 * @author Hadryan Silva
 * @since 21-03-2025
 */
public class MessageUtil {

    /**
     * Exibe uma mensagem de informação
     *
     * @param parent Componente pai
     * @param message Mensagem a ser exibida
     * @param title Título da janela
     */
    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exibe uma mensagem de erro
     *
     * @param parent Componente pai
     * @param message Mensagem a ser exibida
     * @param title Título da janela
     */
    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Exibe uma mensagem de alerta
     *
     * @param parent Componente pai
     * @param message Mensagem a ser exibida
     * @param title Título da janela
     */
    public static void showWarning(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Exibe uma mensagem de confirmação
     *
     * @param parent Componente pai
     * @param message Mensagem a ser exibida
     * @param title Título da janela
     * @return true se o usuário confirmar, false caso contrário
     */
    public static boolean showConfirm(Component parent, String message, String title) {
        int response = JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return response == JOptionPane.YES_OPTION;
    }

    /**
     * Exibe uma mensagem de confirmação com opções personalizadas
     *
     * @param parent Componente pai
     * @param message Mensagem a ser exibida
     * @param title Título da janela
     * @param options Array de opções para exibição
     * @param defaultOption Opção padrão selecionada
     * @return Índice da opção selecionada, ou -1 se o diálogo for fechado
     */
    public static int showOptionDialog(Component parent, String message, String title,
                                       Object[] options, Object defaultOption) {
        return JOptionPane.showOptionDialog(parent, message, title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, defaultOption);
    }
}
