package calorize.controller;

import calorize.dao.RefeicaoDAO;
import calorize.dao.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import calorize.model.Alimento;
import calorize.model.MetaMacros;
import calorize.model.Usuario;
import calorize.resources.RingProgressIndicator;
import calorize.utils.Contexto;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>Controlador da tela principal da aplicação, exibindo um resumo das informações
 * do usuário, como o progresso calórico do dia, as metas de macronutrientes,
 * e um histórico de peso.</p>
 *
 * <p>Esta classe gerencia a exibição de dados dinâmicos na interface, buscando
 * informações do usuário logado e dados de refeições para apresentar um panorama
 * diário e histórico.</p>
 *
 * <p>Integra-se com {@link Contexto} para o estado global, {@link UsuarioDAO}
 * para dados do usuário e {@link RefeicaoDAO} para dados de refeições.</p>
 */
public class telaPrincipalController {
    private final UsuarioDAO usuarioDAO = UsuarioDAO.getInstance(); // Instância única do DAO de Usuário

    @FXML
    private Button editarInformacoesButton;
    @FXML
    private Button irParaTelaRefeicoesButton;
    @FXML
    private Button irParaTelaTabelaButton;
    @FXML
    private Button irParaTelaAdicionarAmigosButton;

    @FXML
    private LineChart<String, Number> graficoRegistrosPeso; // Gráfico para o histórico de peso

    @FXML
    private Label proteinaHoje; // Rótulo para exibir a proteína consumida hoje
    @FXML
    private Label carboidratosHoje; // Rótulo para exibir os carboidratos consumidos hoje
    @FXML
    private Label gordurasHoje; // Rótulo para exibir as gorduras consumidas hoje

    @FXML
    private RingProgressIndicator progressoCalorias; // Indicador de progresso para calorias

    private final RefeicaoDAO refeicaoDAO = new RefeicaoDAO(); // Instância do DAO de Refeição

    private Usuario usuario; // Referência ao usuário logado

    /**
     * <p>Método de inicialização do controlador, invocado automaticamente após o carregamento do FXML.</p>
     *
     * <p>Responsável por carregar o usuário logado do {@link Contexto}, buscar o histórico
     * de peso do usuário, carregar as metas de macronutrientes, os macros consumidos no dia
     * e popular o gráfico de histórico de peso.</p>
     */
    @FXML
    public void initialize() {
        Optional<Usuario> optionalUsuario = Contexto.getUsuarioLogado(); // Pega o Optional

        if (optionalUsuario.isPresent()) {
            usuario = optionalUsuario.get(); // Obtém o Usuario do Optional
            // Garante que o histórico de peso do usuário em memória esteja atualizado
            // Cuidado com o cast aqui! Certifique-se de que getPesosPorUsuario realmente retorna Map<LocalDate, Double>
            // ou trate um possível ClassCastException.
            // Se getPesosPorUsuario já retorna o tipo correto, o cast é desnecessário.
            Map<LocalDate, Double> pesos = (Map<LocalDate, Double>) usuarioDAO.getPesosPorUsuario(usuario.getId());
            if (pesos != null) {
                usuario.setHistoricoPeso(pesos);
            } else {
                System.err.println("Atenção: Histórico de peso retornado como null para o usuário " + usuario.getId());
                usuario.setHistoricoPeso(Collections.emptyMap()); // Define um mapa vazio para evitar NullPointerException
            }
            carregarMetasMacros();
            carregarMacros();
            carregarGraficoPeso();
        } else {
            System.err.println("Erro: Nenhum usuário logado. Não é possível inicializar a tela principal.");
            // Desabilite botões ou campos se a tela não puder funcionar sem um usuário logado
            if (editarInformacoesButton != null) editarInformacoesButton.setDisable(true);
            if (irParaTelaRefeicoesButton != null) irParaTelaRefeicoesButton.setDisable(true);
            if (irParaTelaTabelaButton != null) irParaTelaTabelaButton.setDisable(true);
            if (irParaTelaAdicionarAmigosButton != null) irParaTelaAdicionarAmigosButton.setDisable(true);

            // Também inicialize o indicador de progresso para evitar NullPointerExceptions em carregarMacros()
            if (progressoCalorias != null) {
                progressoCalorias.setProgress(0);
                progressoCalorias.setLabel("N/A kcal");
            }
            // Defina N/A para os rótulos de macros também
            if (proteinaHoje != null) proteinaHoje.setText("N/A");
            if (carboidratosHoje != null) carboidratosHoje.setText("N/A");
            if (gordurasHoje != null) gordurasHoje.setText("N/A");
            if (metaProteina != null) metaProteina.setText("N/A");
            if (metaCarboidratos != null) metaCarboidratos.setText("N/A");
            if (metaGorduras != null) metaGorduras.setText("N/A");
        }
    }

    @FXML
    private Label metaProteina, metaCarboidratos, metaGorduras; // Rótulos para exibir as metas de macros

    /**
     * <p>Carrega e exibe as metas diárias de macronutrientes (proteínas, carboidratos, gorduras)
     * e calorias do usuário logado.</p>
     *
     * <p>Utiliza os métodos de cálculo de metas da classe {@link Usuario} e atualiza os
     * respectivos rótulos na interface. Em caso de erro de cálculo, define valores padrão.</p>
     */
    public void carregarMetasMacros() {
        if (usuario == null) {
            System.err.println("Usuário não carregado. Não é possível carregar metas de macros.");
            metaProteina.setText("N/A");
            metaCarboidratos.setText("N/A");
            metaGorduras.setText("N/A");
            return;
        }

        try {
            int idade = 25;
            double pesoAtual = usuario.getPesoAtual() != null ? usuario.getPesoAtual() : 0; // Obtém o peso atual de forma segura

            int metaCal = usuario.calcularMetaCalorias(idade);
            double prot = usuario.calcularMetaProteinas(pesoAtual);
            double gord = usuario.calcularMetaGorduras(metaCal, pesoAtual);
            double carb = usuario.calcularMetaCarboidratos(metaCal, prot, gord);

            metaProteina.setText(String.format("%.0f g", prot));
            metaCarboidratos.setText(String.format("%.0f g", carb));
            metaGorduras.setText(String.format("%.0f g", gord));

            usuario.setMetaMacros(new MetaMacros(metaCal, prot, carb, gord));

        } catch (ArithmeticException e) {
            System.err.println("Erro no cálculo de metas: " + e.getMessage());
            metaProteina.setText("0 g");
            metaCarboidratos.setText("0 g");
            metaGorduras.setText("0 g");
            usuario.setMetaMacros(new MetaMacros(0, 0, 0, 0));
        }
    }

    /**
     * <p>Carrega e exibe o total de macronutrientes (proteínas, carboidratos, gorduras)
     * e calorias consumidos pelo usuário na calorize.data atual.</p>
     *
     * <p>Busca os alimentos de todas as refeições do dia corrente no banco de dados,
     * soma seus valores nutricionais e atualiza os rótulos correspondentes na interface.
     * Também atualiza o indicador de progresso calórico com base na meta diária.</p>
     */
    private void carregarMacros() {
        if (usuario == null) {
            System.err.println("Usuário não carregado. Não é possível carregar macros.");
            proteinaHoje.setText("N/A");
            carboidratosHoje.setText("N/A");
            gordurasHoje.setText("N/A");
            progressoCalorias.setProgress(0);
            progressoCalorias.setLabel("N/A kcal");
            return;
        }

        try {
            String dataHoje = LocalDate.now().toString();

            double caloriasTotais = 0;
            double proteinasTotais = 0;
            double carboidratosTotais = 0;
            double gordurasTotais = 0;

            String[] tiposRefeicoes = {"cafe", "almoco", "jantar", "lanches"};

            for (String tipo : tiposRefeicoes) {
                List<Alimento> alimentos = refeicaoDAO.buscarAlimentosPorRefeicao(
                        usuario.getId(), tipo, dataHoje);

                for (Alimento alimento : alimentos) {
                    caloriasTotais += alimento.getCalorias();
                    proteinasTotais += alimento.getProteinas();
                    carboidratosTotais += alimento.getCarboidratos();
                    gordurasTotais += alimento.getGorduras();
                }
            }

            proteinaHoje.setText(String.format("%.1f g", proteinasTotais));
            carboidratosHoje.setText(String.format("%.1f g", carboidratosTotais));
            gordurasHoje.setText(String.format("%.1f g", gordurasTotais));

            MetaMacros metas = usuario.getMetaMacros();
            double metaCalorias = metas.getCalorias();

            if (metaCalorias > 0) {
                // progresso é um double de 0.0 a 1.0
                double progresso = Math.min(caloriasTotais / metaCalorias, 1.0);
                // Define o progresso no RingProgressIndicator como double, não int
                progressoCalorias.setProgress((int) progresso); // <-- FIX: Changed (int) progresso to progresso
                progressoCalorias.setLabel(String.format("%.0f/%.0f kcal", caloriasTotais, metaCalorias));
            } else {
                progressoCalorias.setProgress(0); // Use 0.0 for double type
                progressoCalorias.setLabel("0/0 kcal");
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar macros: " + e.getMessage());
            e.printStackTrace(); // Added for better debugging
            proteinaHoje.setText("0 g");
            carboidratosHoje.setText("0 g");
            gordurasHoje.setText("0 g");
            progressoCalorias.setProgress(0); // Use 0.0 for double type
            progressoCalorias.setLabel("0/0 kcal");
        }
    }

    /**
     * <p>Carrega e exibe o histórico de peso do usuário no gráfico de linha.</p>
     *
     * <p>Os dados são obtidos do histórico de peso do objeto {@link Usuario} (que é populado
     * na inicialização do controlador) e são plotados no {@link LineChart}, ordenados por calorize.data.</p>
     */
    private void carregarGraficoPeso() {
        if (usuario == null) {
            System.err.println("Usuário não carregado. Não é possível carregar o gráfico de peso.");
            return;
        }

        graficoRegistrosPeso.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Peso (kg)");

        if (usuario.getHistoricoPeso() != null && !usuario.getHistoricoPeso().isEmpty()) {
            usuario.getHistoricoPeso().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()) // Use comparingByKey for LocalDate keys
                    .forEach(entry -> {
                        String dataFormatada = entry.getKey().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));
                        series.getData().add(new XYChart.Data<>(dataFormatada, entry.getValue()));
                    });
        }

        graficoRegistrosPeso.getData().add(series);
    }

    /**
     * <p>Manipula o evento de clique no botão "Editar Informações".</p>
     *
     * <p>Redireciona o usuário para a tela de edição de informações de perfil.</p>
     *
     * @param event O {@link javafx.event.ActionEvent} que disparou esta ação.
     * @throws IOException Se houver um erro ao carregar o arquivo FXML da tela de edição.
     */
    @FXML
    private void irParaTelaEditarInformacoes(javafx.event.ActionEvent event) throws IOException {
        new SceneController().switchScene(event, "/fxml/telaEditarInformacoes.fxml");
    }

    /**
     * <p>Manipula o evento de clique no botão "Refeições".</p>
     *
     * <p>Redireciona o usuário para a tela de gerenciamento de refeições.</p>
     *
     * @param event O {@link javafx.event.ActionEvent} que disparou esta ação.
     * @throws IOException Se houver um erro ao carregar o arquivo FXML da tela de refeições.
     */
    @FXML
    private void irParaTelaRefeicoes(javafx.event.ActionEvent event) throws IOException {
        new SceneController().switchScene(event, "/fxml/telaRefeicoes.fxml");
    }

    /**
     * <p>Manipula o evento de clique no botão "Tabela".</p>
     *
     * <p>Redireciona o usuário para a tela que exibe uma tabela de alimentos.</p>
     *
     * @param event O {@link javafx.event.ActionEvent} que disparou esta ação.
     * @throws IOException Se houver um erro ao carregar o arquivo FXML da tela de tabela.
     */
    @FXML
    private void irParaTelaTabela(javafx.event.ActionEvent event) throws IOException {
        new SceneController().switchScene(event, "/fxml/telaTabela.fxml");
    }

    /**
     * <p>Manipula o evento de clique no botão "Adicionar Amigos".</p>
     *
     * <p>Redireciona o usuário para a tela de gerenciamento de amigos.</p>
     *
     * @param event O {@link javafx.event.ActionEvent} que disparou esta ação.
     * @throws IOException Se houver um erro ao carregar o arquivo FXML da tela de adicionar amigos.
     */
    @FXML
    private void irParaTelaAdicionarAmigos(javafx.event.ActionEvent event) throws IOException {
        new SceneController().switchScene(event, "/fxml/telaAdicionarAmigos.fxml");
    }
}