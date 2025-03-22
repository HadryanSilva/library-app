package br.com.hadryan.app.view.components;

import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.view.MainFrame;
import br.com.hadryan.app.view.dialog.LivroFormDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * Painel para listagem e gerenciamento de livros.
 */
public class LivroListaPainel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame janelaPrincipal;
    private final LivroController livroController;

    private LivroTable livroTable;
    private JButton adicionarButton;
    private JButton editarButton;
    private JButton excluirButton;
    private JButton atualizarButton;

    /**
     * Construtor do painel de listagem
     */
    public LivroListaPainel(MainFrame janelaPrincipal, LivroController livroController) {
        this.janelaPrincipal = janelaPrincipal;
        this.livroController = livroController;

        initComponents();
        layoutComponents();
        setupListeners();
        atualizarDados();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        // Tabela de livros
        livroTable = new LivroTable();

        // Botões
        adicionarButton = new JButton("Adicionar Livro");
        editarButton = new JButton("Editar Livro");
        excluirButton = new JButton("Excluir Livro");
        atualizarButton = new JButton("Atualizar");

        // Inicialmente desabilita os botões de edição/exclusão
        editarButton.setEnabled(false);
        excluirButton.setEnabled(false);
    }

    /**
     * Configura o layout do painel
     */
    private void layoutComponents() {
        // Painel de botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(adicionarButton);
        buttonPanel.add(editarButton);
        buttonPanel.add(excluirButton);
        buttonPanel.add(atualizarButton);

        // Adiciona componentes ao painel principal
        add(livroTable, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        // Listener de seleção da tabela
        livroTable.getTable().getSelectionModel().addListSelectionListener(e -> {
            boolean temSelecao = livroTable.getSelectedRow() != -1;
            editarButton.setEnabled(temSelecao);
            excluirButton.setEnabled(temSelecao);
        });

        // Ação de duplo clique na tabela
        livroTable.setOnDoubleClickAction(this::editarLivro);

        // Ações dos botões
        adicionarButton.addActionListener(this::adicionarLivro);
        editarButton.addActionListener(e -> editarLivroSelecionado());
        excluirButton.addActionListener(this::excluirLivroSelecionado);
        atualizarButton.addActionListener(e -> atualizarDados());
    }

    /**
     * Atualiza os dados da tabela
     */
    public void atualizarDados() {
        livroTable.carregarDados(livroController.listarTodos());
    }

    /**
     * Adiciona um novo livro
     */
    private void adicionarLivro(ActionEvent e) {
        LivroFormDialog dialog = new LivroFormDialog(janelaPrincipal, null, livroController);
        dialog.setVisible(true);

        // Atualiza a tabela se o livro foi salvo
        if (dialog.isConfirmado()) {
            atualizarDados();
        }
    }

    /**
     * Edita o livro selecionado
     */
    private void editarLivroSelecionado() {
        Livro livroSelecionado = livroTable.getLivroSelecionado();
        if (livroSelecionado == null) {
            return;
        }

        editarLivro(livroSelecionado);
    }

    /**
     * Edita um livro específico
     */
    private void editarLivro(Livro livro) {
        // Busca o livro completo pelo ID
        Optional<Livro> livroCompleto = livroController.buscarPorId(livro.getId());
        if (!livroCompleto.isPresent()) {
            JOptionPane.showMessageDialog(this,
                    "Livro não encontrado. Ele pode ter sido excluído.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Abre o diálogo de edição
        LivroFormDialog dialog = new LivroFormDialog(janelaPrincipal, livroCompleto.get(), livroController);
        dialog.setVisible(true);

        // Atualiza a tabela se o livro foi editado
        if (dialog.isConfirmado()) {
            atualizarDados();
        }
    }

    /**
     * Exclui o livro selecionado
     */
    private void excluirLivroSelecionado(ActionEvent e) {
        Livro livroSelecionado = livroTable.getLivroSelecionado();
        if (livroSelecionado == null) {
            return;
        }

        // Confirma a exclusão
        int resposta = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir o livro \"" + livroSelecionado.getTitulo() + "\"?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (resposta == JOptionPane.YES_OPTION) {
            try {
                // Exclui o livro
                livroController.excluir(livroSelecionado.getId());
                atualizarDados();
                JOptionPane.showMessageDialog(this,
                        "Livro excluído com sucesso.",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao excluir livro: " + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
