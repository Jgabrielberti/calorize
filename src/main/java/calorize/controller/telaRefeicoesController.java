package calorize.controller;

import calorize.dao.RefeicaoDAO;
import calorize.model.Alimento;
import calorize.utils.Contexto;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Controlador da tela de gerenciamento de refeições, permitindo ao usuário visualizar
 * e adicionar alimentos a diferentes tipos de refeições (café da manhã, almoço, jantar e lanches).</p>
 *
 * <p>Esta classe organiza os alimentos por tipo de refeição em {@link ListView}s distintas
 * e gerencia a navegação para a tela de adição de alimentos, passando o tipo de refeição
 * selecionado através do {@link Contexto}.</p>
 *
 * <p>Utiliza {@link RefeicaoDAO} para buscar e gerenciar os alimentos associados às refeições.</p>
 */
public class telaRefeicoesController {

    @FXML private ListView<String> listCafe;
    @FXML private ListView<String> listAlmoco;
    @FXML private ListView<String> listJantar;
    @FXML private ListView<String> listLanches;

    @FXML private Button adicionarAlimentoCafeDaManhaButton;
    @FXML private Button adicionarAlimentoAlmocoButton;
    @FXML private Button adicionarAlimentoJantarButton;
    @FXML private Button adicionarAlimentoLanchesButton;

    private final RefeicaoDAO refeicaoDAO = new RefeicaoDAO();
    private final SceneController sceneController = new SceneController();

    // Mapeia botões aos tipos de refeição correspondentes
    private final Map<Button, String> refeicaoPorBotao = new HashMap<>();

    /**
     * <p>Método de inicialização do controlador, invocado automaticamente após o carregamento do FXML.</p>
     *
     * <p>Responsável por mapear cada botão de adição de alimento ao seu respectivo tipo de refeição,
     * o que é usado posteriormente para definir o contexto da refeição ao navegar para a tela
     * de adição de alimentos.</p>
     */
    @FXML
    public void initialize() {
        refeicaoPorBotao.put(adicionarAlimentoCafeDaManhaButton, "cafe");
        refeicaoPorBotao.put(adicionarAlimentoAlmocoButton, "almoco");
        refeicaoPorBotao.put(adicionarAlimentoJantarButton, "jantar");
        refeicaoPorBotao.put(adicionarAlimentoLanchesButton, "lanches");
    }

    /**
     * <p>Manipula o evento de clique em um dos botões de "Adicionar Alimento".</p>
     *
     * <p>Identifica qual botão foi clicado, determina o tipo de refeição associado a ele
     * (e.g., "cafe", "almoco"), define este tipo no {@link Contexto} e navega para a
     * tela de adição de alimentos ({@code telaAdicionarAlimentos.fxml}).</p>
     *
     * @param event O {@link ActionEvent} que disparou esta ação (o clique de um botão).
     */
    @FXML
    private void handleAdicionarAlimento(ActionEvent event) {
        Button source = (Button) event.getSource();
        String tipo = refeicaoPorBotao.get(source);

        if (tipo != null) {
            Contexto.setTipoRefeicao(tipo);
            sceneController.switchScene(event, "/fxml/telaAdicionarAlimentos.fxml");
        } else {
            System.err.println("Erro: Botão não mapeado para um tipo de refeição.");
        }
    }

    /**
     * <p>Carrega e exibe os alimentos consumidos em todas as refeições para uma calorize.data e usuário específicos.</p>
     *
     * <p>Este método invoca {@link #carregarRefeicao(String, ListView, String, int)} para cada tipo
     * de refeição (café da manhã, almoço, jantar, lanches), preenchendo as respectivas {@link ListView}s.</p>
     *
     * @param data A calorize.data (no formato de string) para a qual os alimentos serão carregados.
     * @param usuarioId O ID do usuário cujos alimentos serão buscados.
     */
    public void carregarAlimentosDia(String data, int usuarioId) {
        try {
            carregarRefeicao("cafe", listCafe, data, usuarioId);
            carregarRefeicao("almoco", listAlmoco, data, usuarioId);
            carregarRefeicao("jantar", listJantar, data, usuarioId);
            carregarRefeicao("lanches", listLanches, data, usuarioId);
        } catch (SQLException e) { // Mudado de Exception para SQLException, pois é o tipo de exceção que carregarRefeicao lança
            System.err.println("Erro ao carregar alimentos do dia: " + e.getMessage());
            // Poderia ser interessante exibir um alerta ao usuário aqui
        }
    }

    /**
     * <p>Carrega os alimentos de um tipo de refeição específico para uma determinada calorize.data e usuário.</p>
     *
     * <p>Limpa a {@link ListView} fornecida e a preenche com os nomes dos alimentos
     * recuperados do banco de dados através do {@link RefeicaoDAO}.</p>
     *
     * @param tipo O tipo da refeição (e.g., "cafe", "almoco").
     * @param listView A {@link ListView} que será preenchida com os alimentos.
     * @param data A calorize.data (no formato de string) dos alimentos a serem carregados.
     * @param usuarioId O ID do usuário ao qual os alimentos pertencem.
     * @throws SQLException Se ocorrer um erro de banco de dados durante a busca.
     */
    private void carregarRefeicao(String tipo, ListView<String> listView, String data, int usuarioId) throws SQLException {
        List<Alimento> alimentos = refeicaoDAO.buscarAlimentosPorRefeicao(usuarioId, tipo, data); // <-- AQUI A CORREÇÃO

        listView.getItems().clear();
        for (Alimento alimento : alimentos) {
            listView.getItems().add(alimento.getNome());
        }
    }
}