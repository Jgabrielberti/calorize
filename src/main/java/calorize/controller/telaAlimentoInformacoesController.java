package calorize.controller;

import calorize.dao.RefeicaoDAO;
import calorize.model.Alimento;
import calorize.model.Usuario; // Import Usuario
import calorize.utils.Contexto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Optional; // Import Optional

/**
 * <p>Controlador da tela de informações detalhadas de um alimento, onde o usuário pode
 * visualizar seus macronutrientes e adicioná-lo a uma refeição específica com uma quantidade customizada.</p>
 *
 * <p>Esta classe calcula os valores nutricionais com base na quantidade em gramas informada pelo usuário
 * e exibe essas informações, incluindo um gráfico de pizza para a proporção de macronutrientes.</p>
 *
 * <p>Utiliza {@link Contexto} para obter o alimento selecionado e a {@link RefeicaoDAO} para
 * persistir o alimento na refeição do usuário.</p>
 */
public class telaAlimentoInformacoesController {

    @FXML private Label nomeAlimento;
    @FXML private TextField quantidadeGramas;

    @FXML private Label quantidadeCalorias;
    @FXML private Label quantidadeProteinas;
    @FXML private Label quantidadeCarboidratos;
    @FXML private Label quantidadeGorduras;

    @FXML private PieChart graficoMacros;

    private Alimento alimentoSelecionado;

    /**
     * <p>Método de inicialização do controlador, invocado automaticamente após o carregamento do FXML.</p>
     *
     * <p>Responsável por carregar o alimento que foi selecionado na tela anterior através do {@link Contexto},
     * preencher o nome do alimento na interface e configurar um listener para o campo de entrada de gramas,
     * que recalcula os macros dinamicamente.</p>
     *
     * <p>Exibe uma mensagem de erro se nenhum alimento for encontrado no {@link Contexto}.</p>
     */
    @FXML
    public void initialize() {
        alimentoSelecionado = Contexto.getAlimentoSelecionado();

        if (alimentoSelecionado != null) {
            nomeAlimento.setText(alimentoSelecionado.getNome());

            // Adiciona um listener para recalcular os macros sempre que a quantidade em gramas muda
            quantidadeGramas.textProperty().addListener((obs, oldVal, newVal) -> calcularMacros(newVal));
        } else {
            showErro("Nenhum alimento selecionado.");
        }
    }

    /**
     * <p>Calcula os valores de macronutrientes (calorias, proteínas, carboidratos e gorduras)
     * com base na quantidade em gramas inserida pelo usuário.</p>
     *
     * <p>Atualiza os rótulos correspondentes na interface e o gráfico de pizza de macronutrientes.</p>
     *
     * @param gramasTexto A string contendo a quantidade em gramas inserida pelo usuário.
     */
    private void calcularMacros(String gramasTexto) {
        if (alimentoSelecionado == null) {
            showErro("Nenhum alimento selecionado.");
            return;
        }

        try {
            if (gramasTexto == null || gramasTexto.isEmpty()) {
                limparCampos(); // Limpa os campos se o texto estiver vazio
                return;
            }

            double gramas = Double.parseDouble(gramasTexto);
            double fator = gramas / 100.0; // Fator para proporcionalizar os valores nutricionais (base 100g)

            double kcal = alimentoSelecionado.getCalorias() * fator;
            double prot = alimentoSelecionado.getProteinas() * fator;
            double carb = alimentoSelecionado.getCarboidratos() * fator;
            double gord = alimentoSelecionado.getGorduras() * fator;

            // Atualiza os rótulos com os valores calculados formatados
            quantidadeCalorias.setText(String.format("%.2f", kcal));
            quantidadeProteinas.setText(String.format("%.2f", prot));
            quantidadeCarboidratos.setText(String.format("%.2f", carb));
            quantidadeGorduras.setText(String.format("%.2f", gord));

            // Atualiza o gráfico de pizza
            atualizarGrafico(prot, gord, carb);

        } catch (NumberFormatException e) {
            // Limpa os campos se a entrada não for um número válido
            limparCampos();
        }
    }

    /**
     * <p>Limpa os rótulos que exibem as quantidades de calorias, proteínas, carboidratos e gorduras.</p>
     */
    private void limparCampos() {
        quantidadeCalorias.setText("");
        quantidadeProteinas.setText("");
        quantidadeCarboidratos.setText("");
        quantidadeGorduras.setText("");
    }

    /**
     * <p>Atualiza o {@link PieChart} de macronutrientes com os novos valores de proteínas, gorduras e carboidratos.</p>
     *
     * <p>Define os dados do gráfico, torna a legenda e os rótulos visíveis e aplica cores customizadas às fatias.</p>
     *
     * @param proteinas A quantidade de proteínas a ser exibida no gráfico.
     * @param gorduras A quantidade de gorduras a ser exibida no gráfico.
     * @param carboidratos A quantidade de carboidratos a ser exibida no gráfico.
     */
    private void atualizarGrafico(double proteinas, double gorduras, double carboidratos) {
        // Cria os dados para o gráfico de pizza
        PieChart.Data dataCarbo = new PieChart.Data("Carboidratos (" + String.format("%.1f", carboidratos) + "g)", carboidratos);
        PieChart.Data dataProteina = new PieChart.Data("Proteínas (" + String.format("%.1f", proteinas) + "g)", proteinas);
        PieChart.Data dataGordura = new PieChart.Data("Gorduras (" + String.format("%.1f", gorduras) + "g)", gorduras);

        // Adiciona os dados à lista observável e define-os no gráfico
        ObservableList<PieChart.Data> dados = FXCollections.observableArrayList(dataCarbo, dataProteina, dataGordura);
        graficoMacros.setData(dados);
        graficoMacros.setLegendVisible(true); // Exibe a legenda
        graficoMacros.setLabelsVisible(true); // Exibe os rótulos

        // Aplica estilos CSS para definir as cores das fatias
        // Make sure these colors match your application's theme if you have one
        dataCarbo.getNode().setStyle("-fx-pie-color: #03DAC5;");
        dataProteina.getNode().setStyle("-fx-pie-color: #eb7c0d;");
        dataGordura.getNode().setStyle("-fx-pie-color: #BB86FC;");
    }

    /**
     * <p>Manipula o evento de adicionar o alimento selecionado à refeição do usuário.</p>
     *
     * <p>Valida a quantidade em gramas inserida, cria uma nova instância de {@link Alimento}
     * com os valores nutricionais ajustados pela quantidade, e utiliza {@link RefeicaoDAO}
     * para persistir este alimento na base de dados, associado ao usuário logado e ao tipo de refeição.</p>
     *
     * <p>Após a adição bem-sucedida, navega para a tela de refeições.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     */
    @FXML
    private void handleAdicionarAlimentoARefeicao(ActionEvent event) {
        if (alimentoSelecionado == null) {
            showErro("Nenhum alimento selecionado.");
            return;
        }

        try {
            double gramas = Double.parseDouble(quantidadeGramas.getText());
            if (gramas <= 0) {
                showErro("A quantidade em gramas deve ser um número positivo.");
                return;
            }

            // Correctly handle the Optional<Usuario>
            Optional<Usuario> optionalUsuario = Contexto.getUsuarioLogado();
            String tipo = Contexto.getTipoRefeicao();

            if (optionalUsuario.isEmpty() || tipo == null) {
                showErro("Erro ao recuperar dados do usuário ou tipo de refeição. Por favor, faça login ou selecione a refeição novamente.");
                // Optionally, redirect to login or previous screen
                return;
            }
            Usuario usuario = optionalUsuario.get(); // Get the Usuario object

            double fator = gramas / 100.0;

            // Cria um novo objeto Alimento com os macros recalculados para a quantidade informada
            Alimento alimentoComQuantidade = new Alimento(
                    alimentoSelecionado.getId(),
                    alimentoSelecionado.getNome(),
                    alimentoSelecionado.getCategoria(),
                    alimentoSelecionado.getCalorias() * fator,
                    alimentoSelecionado.getProteinas() * fator,
                    alimentoSelecionado.getGorduras() * fator,
                    alimentoSelecionado.getCarboidratos() * fator,
                    alimentoSelecionado.getFibras() * fator,
                    alimentoSelecionado.getCalcio() * fator
            );

            RefeicaoDAO dao = new RefeicaoDAO();
            boolean sucesso = dao.adicionarAlimento(
                    usuario.getId(), // Now this is correct
                    tipo,
                    alimentoComQuantidade
            );

            if (sucesso) {
                showMensagem("Sucesso", "Alimento adicionado com sucesso!", Alert.AlertType.INFORMATION);
                // Navega de volta para a tela de refeições
                new SceneController().switchScene(event, "/fxml/telaRefeicoes.fxml");
            } else {
                showErro("Erro ao adicionar alimento. Tente novamente.");
            }

        } catch (NumberFormatException e) {
            showErro("Digite uma quantidade válida em gramas (apenas números).");
        } catch (Exception e) {
            // Captura qualquer outra exceção inesperada
            showErro("Ocorreu um erro inesperado ao adicionar o alimento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * <p>Manipula o evento de voltar para a tela de adicionar alimentos.</p>
     *
     * <p>Redireciona o usuário de volta para a tela anterior utilizando o {@link SceneController}.</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (geralmente um clique de botão).
     * @throws IOException Se houver um erro ao carregar o arquivo FXML da tela de adicionar alimentos.
     */
    @FXML
    private void voltarParaTelaAdicionarAlimento(ActionEvent event) throws IOException {
        new SceneController().switchScene(event, "/fxml/telaAdicionarAlimentos.fxml");
    }

    /**
     * <p>Exibe uma caixa de diálogo de alerta com uma mensagem de erro.</p>
     *
     * @param mensagem A mensagem de erro a ser exibida.
     */
    private void showErro(String mensagem) {
        showMensagem("Erro", mensagem, Alert.AlertType.ERROR);
    }

    /**
     * <p>Exibe uma caixa de diálogo de alerta genérica com título, mensagem e tipo de alerta especificados.</p>
     *
     * @param titulo O título da caixa de diálogo.
     * @param mensagem A mensagem a ser exibida na caixa de diálogo.
     * @param tipo O tipo de alerta (e.g., {@link Alert.AlertType#INFORMATION}, {@link Alert.AlertType#ERROR}).
     */
    private void showMensagem(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}