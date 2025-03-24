package br.com.hadryan.app.view.components.panel;

import br.com.hadryan.app.controller.LivroController;
import br.com.hadryan.app.model.entity.Autor;
import br.com.hadryan.app.model.entity.Livro;
import br.com.hadryan.app.view.MainFrame;
import br.com.hadryan.app.view.components.base.BaseCrudPanel;
import br.com.hadryan.app.view.components.base.BaseTable;
import br.com.hadryan.app.view.components.dialog.LivroDetailsDialog;
import br.com.hadryan.app.view.components.dialog.LivroFormDialog;

import javax.swing.*;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Painel refatorado para listagem e gerenciamento de livros
 *
 * @author Hadryan Silva
 * @since 23-03-2025
 */
public class LivroListaPanel extends BaseCrudPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame janelaPrincipal;
    private final LivroController livroController;

    private BaseTable<Livro> livroTable;
    private JButton editarButton;
    private JButton excluirButton;
    private JButton visualizarButton;

    /**
     * Construtor do painel de listagem
     */
    public LivroListaPanel(MainFrame janelaPrincipal, LivroController livroController) {
        this.janelaPrincipal = janelaPrincipal;
        this.livroController = livroController;

        initComponents();
        setupListeners();
        updateData();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        String[] colunas = {"ID", "Título", "Autores", "ISBN", "Editora", "Data de Publicação"};
        livroTable = new BaseTable<>(colunas);

        livroTable.setOnDoubleClickAction(this::editarLivro);

        setMainComponent(livroTable);

        addActionButton("Adicionar Livro", e -> adicionarLivro());
        editarButton = addActionButton("Editar Livro", e -> editarLivroSelecionado());
        excluirButton = addActionButton("Excluir Livro", e -> excluirLivroSelecionado());

        visualizarButton = addActionButton("Visualizar Detalhes", e -> visualizarLivroSelecionado());

        addActionButton("Atualizar", e -> updateData());

        editarButton.setEnabled(false);
        excluirButton.setEnabled(false);
        visualizarButton.setEnabled(false);
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        livroTable.addSelectionListener(livro -> {
            boolean temSelecao = livro != null;
            editarButton.setEnabled(temSelecao);
            excluirButton.setEnabled(temSelecao);
            visualizarButton.setEnabled(temSelecao);
        });
    }

    /**
     * Atualiza os dados da tabela
     */
    @Override
    public void updateData() {
        livroTable.setData(livroController.listarTodos(), this::livroParaLinha);
    }

    /**
     * Converte um livro para uma linha da tabela
     */
    private Object[] livroParaLinha(Livro livro) {
        String autores = livro.getAutores().stream()
                .map(Autor::getNome)
                .collect(Collectors.joining(", "));

        return new Object[]{
                livro.getId(),
                livro.getTitulo(),
                autores,
                livro.getIsbn(),
                livro.getEditora() != null ? livro.getEditora().getNome() : "",
                livro.getDataPublicacao() != null ? livro.getDataPublicacao() : ""
        };
    }

    /**
     * Adiciona um novo livro
     */
    private void adicionarLivro() {
        LivroFormDialog dialog = new LivroFormDialog(janelaPrincipal, null, livroController);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            updateData();
        }
    }

    /**
     * Edita o livro selecionado
     */
    private void editarLivroSelecionado() {
        Livro livroSelecionado = livroTable.getSelectedItem();
        if (livroSelecionado == null) {
            return;
        }

        editarLivro(livroSelecionado);
    }

    /**
     * Edita um livro específico
     */
    private void editarLivro(Livro livro) {
        Optional<Livro> livroCompleto = livroController.buscarPorId(livro.getId());
        if (!livroCompleto.isPresent()) {
            showError("Livro não encontrado. Ele pode ter sido excluído.");
            return;
        }

        LivroFormDialog dialog = new LivroFormDialog(janelaPrincipal, livroCompleto.get(), livroController);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            updateData();
        }
    }

    /**
     * Exclui o livro selecionado
     */
    private void excluirLivroSelecionado() {
        Livro livroSelecionado = livroTable.getSelectedItem();
        if (livroSelecionado == null) {
            return;
        }

        if (!confirmAction("Tem certeza que deseja excluir o livro \"" + livroSelecionado.getTitulo() + "\"?")) {
            return;
        }

        try {
            livroController.excluir(livroSelecionado.getId());
            updateData();
            showInfo("Livro excluído com sucesso.");
        } catch (Exception ex) {
            showError("Erro ao excluir livro: " + ex.getMessage());
        }
    }

    /**
     * Visualiza os detalhes do livro selecionado
     */
    private void visualizarLivroSelecionado() {
        Livro livroSelecionado = livroTable.getSelectedItem();
        if (livroSelecionado == null) {
            return;
        }

        Optional<Livro> livroCompleto = livroController.buscarPorId(livroSelecionado.getId());
        if (!livroCompleto.isPresent()) {
            showError("Livro não encontrado. Ele pode ter sido excluído.");
            return;
        }

        LivroDetailsDialog dialog = new LivroDetailsDialog(janelaPrincipal, livroCompleto.get());
        dialog.setVisible(true);
    }
}