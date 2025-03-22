package br.com.hadryan.app.view.components;

import br.com.hadryan.app.service.importacao.ImportService;
import br.com.hadryan.app.view.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Painel para importação de livros a partir de arquivos.
 */
public class ImportacaoPainel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame janelaPrincipal;
    private final ImportService importService;

    // Componentes da UI
    private JTextField caminhoArquivoField;
    private JButton selecionarButton;
    private JButton importarButton;
    private JButton voltarButton;
    private JTextArea logArea;

    /**
     * Construtor do painel de importação
     */
    public ImportacaoPainel(MainFrame janelaPrincipal, ImportService importService) {
        this.janelaPrincipal = janelaPrincipal;
        this.importService = importService;

        initComponents();
        layoutComponents();
        setupListeners();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        // Campo de seleção de arquivo
        caminhoArquivoField = new JTextField(30);
        caminhoArquivoField.setEditable(false);

        selecionarButton = new JButton("Selecionar...");
        importarButton = new JButton("Importar");
        voltarButton = new JButton("Voltar à Lista");

        // Área de log
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);

        // Inicialmente desabilita o botão de importar
        importarButton.setEnabled(false);
    }

    /**
     * Configura o layout do painel
     */
    private void layoutComponents() {
        // Painel de seleção de arquivo
        JPanel filePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        filePanel.add(new JLabel("Arquivo para Importação:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        filePanel.add(caminhoArquivoField, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        filePanel.add(selecionarButton, gbc);

        // Painel de informações
        JPanel infoPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        infoPanel.add(new JLabel("Formatos de arquivo suportados:"), gbc);

        gbc.gridy = 1;
        infoPanel.add(new JLabel("- CSV (valores separados por vírgulas)"), gbc);

        gbc.gridy = 2;
        infoPanel.add(new JLabel("- XML (eXtensible Markup Language)"), gbc);

        gbc.gridy = 3;
        infoPanel.add(new JLabel("Observação: Durante a importação, livros existentes serão atualizados."), gbc);

        // Painel superior
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filePanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        // Painel de botões inferior
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(importarButton);
        buttonPanel.add(voltarButton);

        // Layout principal
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        selecionarButton.addActionListener(e -> selecionarArquivo());
        importarButton.addActionListener(e -> importarArquivo());
        voltarButton.addActionListener(e -> janelaPrincipal.mostrarPainel("LISTA"));
    }

    /**
     * Exibe um seletor de arquivos para escolher o arquivo a importar
     */
    private void selecionarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Arquivo para Importação");

        // Configura filtros de arquivo
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter(
                "Arquivos CSV (*.csv)", "csv");
        FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter(
                "Arquivos XML (*.xml)", "xml");

        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.addChoosableFileFilter(xmlFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(csvFilter);

        // Exibe diálogo de seleção
        int resultado = fileChooser.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivoSelecionado = fileChooser.getSelectedFile();
            caminhoArquivoField.setText(arquivoSelecionado.getAbsolutePath());
            importarButton.setEnabled(true);
            logArea.setText(""); // Limpa a área de log
        }
    }

    /**
     * Importa livros do arquivo selecionado
     */
    private void importarArquivo() {
        String caminhoArquivo = caminhoArquivoField.getText();
        if (caminhoArquivo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um arquivo para importar.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        File arquivoImportacao = new File(caminhoArquivo);
        if (!arquivoImportacao.exists() || !arquivoImportacao.isFile()) {
            JOptionPane.showMessageDialog(this,
                    "O arquivo selecionado não existe.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Desabilita o botão de importar durante a importação
        importarButton.setEnabled(false);
        logArea.setText("Iniciando importação...\n");

        // Usa SwingWorker para realizar a importação em segundo plano
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return realizarImportacao(arquivoImportacao);
            }

            @Override
            protected void done() {
                try {
                    int quantidade = get();
                    logArea.append("Importação concluída. " + quantidade + " livros importados ou atualizados.\n");
                    JOptionPane.showMessageDialog(ImportacaoPainel.this,
                            quantidade + " livros importados ou atualizados com sucesso.",
                            "Importação Concluída",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Atualiza a lista de livros
                    janelaPrincipal.atualizarListaLivros();
                } catch (Exception e) {
                    logArea.append("Erro durante a importação: " + e.getMessage() + "\n");
                    JOptionPane.showMessageDialog(ImportacaoPainel.this,
                            "Erro durante a importação: " + e.getMessage(),
                            "Erro de Importação",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    importarButton.setEnabled(true);
                }
            }
        }.execute();
    }

    /**
     * Realiza o processo de importação
     */
    private int realizarImportacao(File arquivo) throws IOException {
        logArea.append("Importando arquivo: " + arquivo.getName() + "\n");

        // Obtém a extensão do arquivo
        String nomeArquivo = arquivo.getName().toLowerCase();
        String tipoArquivo = nomeArquivo.endsWith(".csv") ? "CSV" :
                nomeArquivo.endsWith(".xml") ? "XML" : "Desconhecido";

        logArea.append("Tipo de arquivo detectado: " + tipoArquivo + "\n");
        logArea.append("Processando...\n");

        return importService.importarLivros(arquivo);
    }
}