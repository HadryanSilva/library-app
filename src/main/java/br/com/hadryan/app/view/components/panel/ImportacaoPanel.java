package br.com.hadryan.app.view.components.panel;

import br.com.hadryan.app.service.importacao.ImportService;
import br.com.hadryan.app.view.MainFrame;
import br.com.hadryan.app.view.components.base.BaseCrudPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Painel refatorado para importação de livros a partir de arquivos.
 */
public class ImportacaoPanel extends BaseCrudPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame janelaPrincipal;
    private final ImportService importService;

    private JTextField caminhoArquivoField;
    private JButton selecionarButton;
    private JButton importarButton;
    private JTextArea logArea;

    /**
     * Construtor do painel de importação
     */
    public ImportacaoPanel(MainFrame janelaPrincipal, ImportService importService) {
        this.janelaPrincipal = janelaPrincipal;
        this.importService = importService;

        initComponents();
        setupListeners();
    }

    /**
     * Inicializa os componentes da UI
     */
    private void initComponents() {
        JPanel fileSelectionPanel = createFileSelectionPanel();

        JPanel infoPanel = createInfoPanel();

        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(fileSelectionPanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(logScrollPane, BorderLayout.CENTER);

        setMainComponent(mainPanel);

        importarButton = addActionButton("Importar", e -> importarArquivo());
        addActionButton("Voltar à Lista", e -> janelaPrincipal.mostrarPainel(MainFrame.PAINEL_LISTA));

        importarButton.setEnabled(false);
    }

    /**
     * Cria o painel de seleção de arquivo
     */
    private JPanel createFileSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        caminhoArquivoField = new JTextField(30);
        caminhoArquivoField.setEditable(false);

        selecionarButton = new JButton("Selecionar...");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Arquivo para Importação:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(caminhoArquivoField, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        panel.add(selecionarButton, gbc);

        return panel;
    }

    /**
     * Cria o painel de informações sobre formatos suportados
     */
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Formatos de arquivo suportados:"), gbc);

        gbc.gridy = 1;
        panel.add(new JLabel("- CSV (valores separados por vírgulas)"), gbc);

        gbc.gridy = 2;
        panel.add(new JLabel("- XML (eXtensible Markup Language)"), gbc);

        gbc.gridy = 3;
        panel.add(new JLabel("- TXT (Fixed Width Text)"), gbc);

        gbc.gridy = 4;
        panel.add(new JLabel("Observação: Durante a importação, livros existentes serão atualizados."), gbc);

        return panel;
    }

    /**
     * Configura os listeners de eventos
     */
    private void setupListeners() {
        selecionarButton.addActionListener(e -> selecionarArquivo());
    }

    /**
     * Exibe um seletor de arquivos para escolher o arquivo a importar
     */
    private void selecionarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Arquivo para Importação");

        addFileFilters(fileChooser);

        int resultado = fileChooser.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivoSelecionado = fileChooser.getSelectedFile();
            caminhoArquivoField.setText(arquivoSelecionado.getAbsolutePath());
            importarButton.setEnabled(true);
            logArea.setText("");
        }
    }

    /**
     * Adiciona filtros de arquivo ao seletor de arquivos
     */
    private void addFileFilters(JFileChooser fileChooser) {
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter(
                "Arquivos CSV (*.csv)", "csv");
        FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter(
                "Arquivos XML (*.xml)", "xml");
        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter(
                "Arquivos de Texto (*.txt)", "txt");

        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.addChoosableFileFilter(xmlFilter);
        fileChooser.addChoosableFileFilter(txtFilter);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileFilter(csvFilter);
    }

    /**
     * Importa livros do arquivo selecionado
     */
    private void importarArquivo() {
        String caminhoArquivo = caminhoArquivoField.getText();
        if (caminhoArquivo.isEmpty()) {
            showError("Selecione um arquivo para importar.");
            return;
        }

        File arquivoImportacao = new File(caminhoArquivo);
        if (!arquivoImportacao.exists() || !arquivoImportacao.isFile()) {
            showError("O arquivo selecionado não existe.");
            return;
        }

        importarButton.setEnabled(false);
        logArea.setText("Iniciando importação...\n");

        new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                publish("Importando arquivo: " + arquivoImportacao.getName());

                String nomeArquivo = arquivoImportacao.getName().toLowerCase();
                String tipoArquivo = detectarTipoArquivo(nomeArquivo);

                publish("Tipo de arquivo detectado: " + tipoArquivo);
                publish("Processando...");

                return realizarImportacao(arquivoImportacao);
            }

            @Override
            protected void process(List<String> chunks) {
                for (String texto : chunks) {
                    logArea.append(texto + "\n");
                }
            }

            @Override
            protected void done() {
                try {
                    int quantidade = get();
                    String mensagem = "Importação concluída. " + quantidade + " livros importados ou atualizados.";
                    logArea.append(mensagem + "\n");
                    showInfo(quantidade + " livros importados ou atualizados com sucesso.");

                    janelaPrincipal.atualizarListaLivros();
                } catch (Exception e) {
                    String mensagemErro = "Erro durante a importação: " + e.getMessage();
                    logArea.append(mensagemErro + "\n");
                    showError(mensagemErro);
                } finally {
                    importarButton.setEnabled(true);
                }
            }
        }.execute();
    }

    /**
     * Detecta o tipo de arquivo com base na extensão
     */
    private String detectarTipoArquivo(String nomeArquivo) {
        if (nomeArquivo.endsWith(".csv")) {
            return "CSV (Valores Separados por Vírgula)";
        } else if (nomeArquivo.endsWith(".xml")) {
            return "XML (eXtensible Markup Language)";
        } else if (nomeArquivo.endsWith(".txt") || nomeArquivo.endsWith(".dat") || nomeArquivo.endsWith(".fix")) {
            return "Texto com Largura Fixa";
        } else {
            return "Desconhecido";
        }
    }

    /**
     * Realiza o processo de importação
     */
    private int realizarImportacao(File arquivo) throws IOException {
        return importService.importarLivros(arquivo);
    }

    /**
     * Implementação do método abstrato da classe base
     */
    @Override
    public void updateData() {
        // Não há dados a carregar inicialmente
    }
}